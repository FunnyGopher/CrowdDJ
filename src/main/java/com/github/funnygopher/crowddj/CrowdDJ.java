package com.github.funnygopher.crowddj;

import com.github.funnygopher.crowddj.h2.DBConfig;
import com.github.funnygopher.crowddj.jetty.PlaybackHandler;
import com.github.funnygopher.crowddj.vlc.NoVLCConnectionException;
import com.github.funnygopher.crowddj.vlc.VLC;
import com.github.funnygopher.crowddj.vlc.VLCPlaylist;
import com.github.funnygopher.crowddj.vlc.VLCStatus;
import org.eclipse.jetty.server.Server;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;

import static com.github.funnygopher.crowddj.jooq.Tables.*;

public class CrowdDJ {

    private VLC vlc;
    private int port;
    private String password;

    private Server server;

    private String vlcPath;
    private boolean validVLCPath;

    private VLCPlaylist playlist;

    public CrowdDJ() {
        this(8081, "root");
    }

    public CrowdDJ(int port, String password) {
        this.port = port;
        this.password = password;

        vlc = new VLC(8080, password);
        validVLCPath = false;

		// Loads any previous playlist into the application
		try (Connection conn = DriverManager.getConnection(DBConfig.DATABASE, DBConfig.USERNAME, DBConfig.PASSWORD)) {
			DSLContext db = DSL.using(conn, SQLDialect.H2);
			Result<Record> result = db.select().from(PLAYLIST).fetch();

			if(result.isEmpty()) {
				System.out.println("There are no records!");
			} else {
				for(Record record : result) {
					vlc.getController().add(new File(record.getValue(PLAYLIST.FILEPATH)));
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}

        playlist = new VLCPlaylist();

        try {
            server = new Server(port);
            server.setHandler(new PlaybackHandler(this));
            server.start();
        } catch (Exception e) {
            System.err.append("Could not start server. Something is wrong...\n");
            e.printStackTrace();
        }
    }

    public VLC getVLC() {
        return vlc;
    }

    public VLCStatus getStatus() {
        return vlc.getStatus();
    }

    public VLCPlaylist getPlaylist() {
        try {
            playlist = vlc.getPlaylist();
        } catch (NoVLCConnectionException e) {
            e.printError("Could not update the playlist. Not connected to VLC media player.");
        }
        return playlist;
    }

    public boolean isVLCConnected() {
        return vlc.getStatus().isConnected();
    }

    public boolean hasValidVLCPath() {
        return validVLCPath;
    }

    public void setVLCPath(String vlcPath) {
        File vlcExe = new File(vlcPath);
        if(vlcExe.isFile()) {
            this.vlcPath = vlcPath;
            validVLCPath = true;
        } else {
            validVLCPath = false;
        }
    }

    public boolean startVLC() {
        if(validVLCPath) {
            return vlc.start(vlcPath);
        }

        return false;
    }

    public void stopServer() {
        if(server.isRunning()) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
