package servlet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import action.Action;
import action.CompleteLiveVideoAction;
import action.CreateLiveVideoAction;
import action.GetFileAciton;
import action.GetFileListAction;
import action.UploadFileAction;
import action.UploadLiveVideoAction;

/**
 * Servlet implementation class TeleMedicineServlet
 */
public class TeleMedicineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Integer GET_FILE_LIST = 1;
	private static final Integer GET_FILE = 2;
	private static final Integer UPLOAD_FILE = 3;
	private static final Integer UPLOAD_LIVE_VIDEO = 4;
	private static final Integer CREATE_LIVE_VIDEO = 5;
	private static final Integer COMPLETE_LIVE_VIDEO = 6;
    private String archiveHome;
    private String videoPath;
    private String imagePath;
    private String pdfPath;
    private String liveVideoPath;
    private ServletContext application;

	private HashMap<Integer, Action> actions;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TeleMedicineServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		application = config.getServletContext();
		archiveHome = application.getInitParameter("ARCHIVE_HOME");
		videoPath = application.getInitParameter("VIDEO_PATH");
		imagePath = application.getInitParameter("IMAGE_PATH");
		pdfPath = application.getInitParameter("PDF_PATH");
		liveVideoPath = application.getInitParameter("LIVE_VIDEO_PATH");
		
		actions = new HashMap<Integer,Action>();
		actions.put(GET_FILE_LIST, new GetFileListAction());
		actions.put(GET_FILE, new GetFileAciton());
		actions.put(UPLOAD_FILE, new UploadFileAction());
		actions.put(UPLOAD_LIVE_VIDEO, new UploadLiveVideoAction());
		actions.put(CREATE_LIVE_VIDEO, new CreateLiveVideoAction());
		actions.put(COMPLETE_LIVE_VIDEO, new CompleteLiveVideoAction());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataInputStream dis = new DataInputStream(request.getInputStream());
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		Integer operator = dis.readInt();
		Action action = actions.get(operator);
		action.execute(dis, dos, this);
		dos.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
	
	public String getArchiveHome() {
		return this.archiveHome;
	}
	
    public String getVideoPath() {
		return archiveHome+videoPath;
	}

	public String getImagePath() {
		return archiveHome+imagePath;
	}
	
	public String getPDFPath() {
		return archiveHome+pdfPath;
	}

	public String getLiveVideoPath() {
		return archiveHome+liveVideoPath;
	}
	
	public ServletContext getServletContext() {
		return application;
	}

}
