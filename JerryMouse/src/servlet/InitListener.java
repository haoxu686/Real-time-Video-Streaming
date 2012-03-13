package servlet;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class InitListener
 *
 */
public class InitListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public InitListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext application =  arg0.getServletContext();
        String archiveHome = application.getInitParameter("ARCHIVE_HOME");
		String videoPath = application.getInitParameter("VIDEO_PATH");
		String imagePath = application.getInitParameter("IMAGE_PATH");
		String pdfPath = application.getInitParameter("PDF_PATH");
		String liveVideoPath = application.getInitParameter("LIVE_VIDEO_PATH");
		File root = new File(archiveHome);
		if (!root.exists()) {
			root.mkdirs();
		}
		File imageDir = new File(root, imagePath);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		File videoDir = new File(root, videoPath);
		if (!videoDir.exists()) {
			videoDir.mkdirs();
		}
		File pdfDir = new File(root, pdfPath);
		if (!pdfDir.exists()) {
			pdfDir.mkdirs();
		}
		File liveVideoDir = new File(root, liveVideoPath);
		if (!liveVideoDir.exists()) {
			liveVideoDir.mkdirs();
		}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    }
	
}
