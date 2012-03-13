package action;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import servlet.TeleMedicineServlet;

public interface Action {

	public void execute(DataInputStream dis, DataOutputStream dos, TeleMedicineServlet servlet);
}
