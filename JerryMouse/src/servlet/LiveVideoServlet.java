package servlet;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.StreamServer;

/**
 * Servlet implementation class LiveVideoServlet
 */
public class LiveVideoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletContext application;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LiveVideoServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		application = config.getServletContext();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		StreamServer server = null;
		while (server == null) {
			server = (StreamServer) application.getAttribute("server");
		}
		String liveVideoPath = server.getLiveVideoPath();
		File file;
		FileInputStream fis;
		int maxSeq;
		int curSeq = 0;
		int bytes = 0;
		byte [] buffer = new byte [8192];
		while (true) {
			maxSeq = server.getCurrentSeq();
			if (maxSeq < curSeq) {
				if (!server.hasMore()) {
					break;
				} else {
					continue;
				}
			}
			while (curSeq <= maxSeq) {
				file = new File(liveVideoPath, curSeq+".3gp");
				fis = new FileInputStream(file);
				while ((bytes = fis.read(buffer)) >= 0) {
					dos.write(buffer, 0, bytes);
				}
				fis.close();
				curSeq++;
			}
		}
		dos.flush();
		dos.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}
