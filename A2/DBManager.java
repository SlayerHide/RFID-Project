package A2;
import java.sql.*;
import java.time.LocalDateTime;

///172.20.10.8 ipServerRemoto
//localhost

public class DBManager {
	private final String url = "jdbc:mysql://192.168.100.103:3306/ClassArcade?useSSL=false&serverTimezone=UTC";
	private final String user = "root";
	private final String pass = "";
	
	public DBManager() throws SQLException{
		 try {
		        Class.forName("com.mysql.cj.jdbc.Driver");
		    } catch (ClassNotFoundException e) {
		        throw new SQLException("No se pudo cargar el driver JDBC", e);
		    } 
	}
	
	private Connection getConn() throws SQLException{
		return DriverManager.getConnection(url,user,pass);
		
		

	}
	
	public synchronized void ensureUserExists(String rf_id,String nickName) throws SQLException{
		try(Connection c = getConn()){
			String q = "select Id from puntos where rf_id = ?";
			try(PreparedStatement ps = c.prepareStatement(q)){
				ps.setString(1, rf_id);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) return;
				}
			}
			//dude i need more time to make this code, if you reading this you're a software developer.
			String insert = "insert into puntos (rf_id,user,score,last_sesion,last_game) values (?,?,?,?,?)";
			try (PreparedStatement ps = c.prepareStatement(insert)){
				ps.setString(1, rf_id);
				ps.setString(2, nickName);
				ps.setInt(3, 0);
				ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
				ps.setString(5, "Space Invaders");
				ps.executeUpdate();
			}
		}
	}
	
	public static class PlayerInfo{
		public final String rf_id;
		public final String username;
		public final int score;
		
		public PlayerInfo(String rf_id, String username, int score) {
			this.rf_id = rf_id;
			this.username = username;
			this.score = score;
		}
		
	}
	
	
	public synchronized PlayerInfo loadPlayerInfo(String rf_id)throws SQLException{
		try(Connection c = getConn()){
			String q = "select user,score from puntos where rf_id = ?";
			try(PreparedStatement ps = c.prepareStatement(q)){
				ps.setString(1,rf_id);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) {
						String userName = rs.getString("user");
						int score = rs.getInt("score");
						return new PlayerInfo(rf_id,userName,score);
					}
				}
			}
		}
		return null;
	}
	
	public synchronized void updateScoreAndSession(String rf_id,int score,String last_game)throws SQLException{
		try(Connection c = getConn()){
			String u = "update puntos SET score = ?, last_sesion = ?, last_game = ? where rf_id = ?";
			try(PreparedStatement ps = c.prepareStatement(u)){
				ps.setInt(1, score);
				ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
				ps.setString(3, last_game);
				ps.setString(4, rf_id);
				ps.executeUpdate();
			}
		}
	}
}

	
	
	


