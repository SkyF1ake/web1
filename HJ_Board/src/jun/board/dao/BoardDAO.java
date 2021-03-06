package jun.board.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.sun.javafx.binding.SelectBinding.AsDouble;

import jun.board.model.BoardDTO;

public class BoardDAO {
	static String dsPath = "java:comp/env/jdbc";
	
	// DB 연결을 확인
	public BoardDAO()
	{
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup(dsPath);
			System.out.println(dataSource+"연결되었습니다");
		} catch (Exception e) {
			System.out.println("DB 연결 실패  : "+e);
			return;
		}
	}
	
	
	// 게시판 글 개수를 확인하는 메소드
	public int getListCount() 
	{
		int i =0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try
		{
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup(dsPath);
			connection = dataSource.getConnection();
			
			//모든 레코드 수를 조회
			String sql = "select count(*) from jboard";
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			
			if(resultSet.next())
			{
				i = resultSet.getInt(1);  
			}
			
		} catch (Exception e) {
			System.out.println("글의 개수 구하기 실패:"+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return i;
	}
	
	// 게시판의 글 목록을 반환
	public List<BoardDTO> getBoardList(int page,int limit) 
	{
		List<BoardDTO> list = new ArrayList<BoardDTO>();
		
		int startrow = (page - 1) * 10+1;
		int endrow = startrow + limit -1;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup(dsPath);
			connection = dataSource.getConnection();
			String sql = "select * from (select rownum rnum, num, name, subject, content,";
			sql += "attached_file,answer_num,answer_lev,answer_seq,read_count,write_date";
			sql += " from (select * from jboard order by answer_num desc,answer_seq asc))";
			sql += " where rnum>=? and rnum <= ?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, startrow);
			preparedStatement.setInt(2, endrow);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next())
			{
				BoardDTO boardDTO = new BoardDTO();
				boardDTO.setNum(resultSet.getInt("num"));
				boardDTO.setName(resultSet.getString("name"));
				boardDTO.setSubject(resultSet.getString("subject"));
				boardDTO.setContent(resultSet.getString("content"));
				boardDTO.setAttached_file(resultSet.getString("attached_file"));
				boardDTO.setAnswer_num(resultSet.getInt("answer_num"));
				boardDTO.setAnswer_lev(resultSet.getInt("answer_lev"));
				boardDTO.setAnswer_seq(resultSet.getInt("answer_seq"));
				boardDTO.setWrite_date(resultSet.getDate("write_date"));
				list.add(boardDTO);
	
			}
			return list;
		} catch (Exception e) {
			System.out.println("글 목록 보기 실패 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				 e.printStackTrace();
			}
		}
		return null;
	}
	
//	 게시판 글 등록 메소드
	public boolean boardInsert(BoardDTO boardDTO) {
		int num = 0;
		String sql = "";
		int result = 0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			
			sql = "select max(num) from jboard";
			preparedStatement = connection.prepareStatement(sql);
			resultSet  = preparedStatement.executeQuery();
			
			if(resultSet.next())
			{
				num = resultSet.getInt(1) + 1;
			} else {
				num = 1;
			}
			preparedStatement.close();
			
			sql = "insert into jboard (num,name,pass,subject,content,attached_file,";
			sql += "answer_num,answer_lev,answer_seq,read_count,write_date)";
			sql += " values(?,?,?,?,?,?,?,?,?,?,sysdate)";
			
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, num);
			preparedStatement.setString(2, boardDTO.getName());
			preparedStatement.setString(3, boardDTO.getPass());
			preparedStatement.setString(4, boardDTO.getSubject());
			preparedStatement.setString(5, boardDTO.getContent());
			preparedStatement.setString(6, boardDTO.getAttached_file());
			preparedStatement.setInt(7, num);
			preparedStatement.setInt(8, 0);
			preparedStatement.setInt(9, 0);
			preparedStatement.setInt(10, 0);
			result = preparedStatement.executeUpdate();
			if(result == 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println("글 등록 실패 : "+e);
		}finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
		
	}
	
	// 번호에 해당하는 글의 정보를 조회하고 내용을 출력
	public BoardDTO getDetail(int num) {
		BoardDTO boardDTO = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet  = null;
		
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			String sql = "select * from jboard where num = ?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, num);
			resultSet = preparedStatement.executeQuery();
			
			if(resultSet.next())
			{
				boardDTO = new BoardDTO();
				boardDTO.setNum(resultSet.getInt("num"));
				boardDTO.setName(resultSet.getString("name"));
				boardDTO.setSubject(resultSet.getString("subject"));
				boardDTO.setContent(resultSet.getString("content"));
				boardDTO.setAttached_file(resultSet.getString("attached_file"));
				boardDTO.setAnswer_num(resultSet.getInt("answer_num"));
				boardDTO.setAnswer_lev(resultSet.getInt("answer_lev"));
				boardDTO.setAnswer_seq(resultSet.getInt("answer_seq"));
				boardDTO.setRead_count(resultSet.getInt("read_count"));
				boardDTO.setWrite_date(resultSet.getDate("write_date"));
			}
			return boardDTO;
		} catch (Exception e) {
			 System.out.println("글 내용 보기 실패 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
	// 글목록에서 클릭시 조회수 증가 메소드
	public void setReadCountUpdate(int num) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			
			String sql = "update jboard set read_count = read_count+1 where num = "+num;
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate();
		} catch (Exception e) {		
			System.out.println("조회수 업데이트 실패 : "+e);
		} finally {
			try {
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 게시판 글의 답변글 등록
	public int boardReply(BoardDTO boardDTO)
	{
		String sql="";
		int num = 0;
		int answer_num = boardDTO.getAnswer_num();
		int answer_lev = boardDTO.getAnswer_lev();
		int answer_seq = boardDTO.getAnswer_seq();
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			sql = "select max(num) from jboard";
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			
			if(resultSet.next()) {
				num = resultSet.getInt(1)+1;
			} else {
				num = 1;
			}
			preparedStatement.close();
			
			sql = "update jboard set answer_seq=answer_seq+1";
			sql += " where answer_num=? and answer_seq>?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, answer_num);
			preparedStatement.setInt(2, answer_seq);
			preparedStatement.executeUpdate();
			answer_seq = answer_seq +1;
			answer_lev = answer_lev +1;
			sql = "insert into jboard (num,name,pass,subject,content,attached_file,";
			sql += "answer_num,answer_lev,answer_seq,read_count,write_date)";
			sql += " values(?,?,?,?,?,?,?,?,?,?,sysdate)";
			preparedStatement = connection.prepareStatement(sql);
		
			preparedStatement.setInt(1, num);
			preparedStatement.setString(2, boardDTO.getName());
			preparedStatement.setString(3, boardDTO.getPass());
			preparedStatement.setString(4, boardDTO.getSubject());
			preparedStatement.setString(5, boardDTO.getContent());
			preparedStatement.setString(6, boardDTO.getAttached_file());
			preparedStatement.setInt(7, answer_num);
			preparedStatement.setInt(8,answer_lev);
			preparedStatement.setInt(9, answer_seq);
			preparedStatement.setInt(10, 0);
			preparedStatement.executeUpdate();
			
			return num;
		} catch(Exception e) {
			System.out.println("글 답변 실패 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				 e.printStackTrace();
			}
		}
		return 0;
	}
	
	
	public boolean boardModify(BoardDTO boardDTO) {
		String fileName = boardDTO.getOld_file();
		String realFolder="";
		realFolder = realFolder+fileName;
		File file = new File(realFolder);
		
		if(boardDTO.getAttached_file()==null)
		{
			boardDTO.setAttached_file(fileName);
		}
		else {
			if(file.exists()) {
				file.delete();
			}
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			String sql = "update jboard set name=?,subject=?,content=?,attached_file=?";
			sql += " where num=?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, boardDTO.getName());
			preparedStatement.setString(2, boardDTO.getSubject());
			preparedStatement.setString(3, boardDTO.getContent());
			preparedStatement.setString(4, boardDTO.getAttached_file());
			preparedStatement.setInt(5, boardDTO.getNum());
			preparedStatement.executeUpdate();
			
			return true;
			
		} catch(Exception e) {
			System.out.println("글 수정 실패 : "+e);
		} finally {
			try {
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// 작성자에 대한 글 비번 조회
	public boolean isBoardWriter(int num,String pass) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			String sql = "select * from jboard where num = ?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, num);
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			if(pass.equals(resultSet.getString("pass")))
			{
				return true;
			}
		} catch (Exception e) {
			 System.out.println("글쓴이 확인 실패 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				 e.printStackTrace();
			}
		}
		return false;
		
	}
	
	public boolean boardDelete(int num) {
		int result = 0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			String sql = "delete from jboard where num=?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, num);
			preparedStatement.executeUpdate();
			if(result == 0) {
				return false;
			}
			return true;
		} catch(Exception e) {
			System.out.println("글 삭제 실패 : "+e);
		} finally {
			try {
				preparedStatement.close();
				connection.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public List<BoardDTO> getSearchList(String keyword,String keyfield,int page,int limit) {
		String searchCall="";
		if(!"".equals(keyword)) {
			if("all".equals(keyfield)) {
				searchCall = "(subject like '%' || '"+keyword+"'||'%') or ( name like '%'||'"+keyword+"'||'%') or ( content like '%' || '"+keyword+"'||'%')";
			} else if("subject".equals(keyfield)) {
				searchCall = " subject like '%' || '"+keyword+"' ||'%'";
			} else if("name".equals(keyfield))
			{
				searchCall = " name like '%'|| '"+keyword+"'||'%'";
			} else if("content".equals(keyfield))
			{
				searchCall = " content like '%'|| '"+keyword+"'||'%'";
			}
		}
		
		List<BoardDTO> list = new ArrayList<BoardDTO>();
		int startrow = (page - 1) * 10 + 1;
		int endrow = startrow + limit - 1;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			
			// 쿼리문 입력후 결과 도출
			String sql = "select * from (select rownum rnum,num,name,subject,content,";
			sql += "attached_file,answer_num,answer_lev,answer_seq,read_count,write_date";
			sql += " from (select * from jboard order by answer_num desc,answer_seq asc)";
			sql += " where "+searchCall+")";
			sql += " where rnum>=? and rnum<=?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, startrow);
			preparedStatement.setInt(2, endrow);
			resultSet = preparedStatement.executeQuery();
			
			// 결과 게시글(컬럼)들 DTO에 set
			while (resultSet.next())
			{
				BoardDTO boardDTO = new BoardDTO();
				boardDTO.setNum(resultSet.getInt("num"));
				boardDTO.setName(resultSet.getString("name"));
				boardDTO.setSubject(resultSet.getString("subject"));
				boardDTO.setContent(resultSet.getString("content"));
				boardDTO.setAttached_file(resultSet.getString("attached_file"));
				boardDTO.setAnswer_num(resultSet.getInt("answer_num"));
				boardDTO.setAnswer_lev(resultSet.getInt("answer_lev"));
				boardDTO.setAnswer_seq(resultSet.getInt("answer_seq"));
				boardDTO.setRead_count(resultSet.getInt("read_count"));
				boardDTO.setWrite_date(resultSet.getDate("write_date"));
				list.add(boardDTO);
			}
			return list;
		} catch (Exception e) {	
			System.out.println("search 에러 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				 e.printStackTrace();
			}
		}
		return null;
	}
	
	public int getSearchListCount(String keyword,String keyfield) {
		String searchCall = "";
		if(!"".equals(keyword))
		{
			if("all".equals(keyfield))
			{
				searchCall = "(subject like '%' || '"+keyword+"' || '%' ) or ( name like '%' || '"+keyword+"'||'%') or (content like '%'||'"+keyword+"'||'%')";
			}
			else if("subject".equals(keyfield))
			{
				searchCall = " subject like '%'"+keyword+"'||'%'";
			}
			else if("name".equals(keyfield))
			{
				searchCall = " name like '%'"+keyword+"'||'%'";	
			}
			else if("content".equals(keyfield))
			{
				searchCall = " content like '%'"+keyword+"'||'%'";	
			}
		}
		int i = 0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc");
			connection = dataSource.getConnection();
			String sql = "select count(*) from jboard where"+searchCall;
			System.out.println("연결이 되었습니다");
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				i = resultSet.getInt(1);
			}
		} catch (Exception e) {
			System.out.println("글의 개수 구하기 실패 : "+e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
}
