package org.imixs.eclipse.manik.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.imixs.eclipse.manik.Console;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;

/**
 * This class provides the workings necessary to connect to a running JVM and to
 * replace classes.
 *
 * @author David A. Kavanagh <a href="mailto:dak@dotech.com">dak@dotech.com</a>
 */
@SuppressWarnings("restriction")
public class HotSwapHelper {

	private VirtualMachine vm;
	
	private Console console;
	
	public HotSwapHelper(Console console) {
		this.console = console;
	}

	public void connect(String name) throws Exception {
		connect(null, null, name);
	}

	public void connect(String host, String port) throws Exception {
		connect(host, port, null);
	}

	// either host,port will be set, or name
	private void connect(String host, String port, String name) throws Exception {
		// connect to JVM
		boolean useSocket = (port != null);
		
		VirtualMachineManager manager = org.eclipse.jdi.Bootstrap.virtualMachineManager();//Bootstrap.virtualMachineManager();
		List<AttachingConnector> connectors = manager.attachingConnectors();
		AttachingConnector connector = null;
		// System.err.println("Connectors available");
		for (int i = 0; i < connectors.size(); i++) {
			AttachingConnector tmp = connectors.get(i);
			// System.err.println("conn "+i+" name="+tmp.name()+"
			// transport="+tmp.transport().name()+
			// " description="+tmp.description());
			if (!useSocket && tmp.transport().name().equals("dt_shmem")) {
				connector = tmp;
				break;
			}
			if (useSocket && tmp.transport().name().equals("dt_socket")) {
				connector = tmp;
				break;
			}
		}
		if (connector == null) {
			throw new IllegalStateException("Cannot find shared memory connector");
		}

		Map<String, Connector.Argument> args = connector.defaultArguments();
		Connector.Argument arg;
		// use name if using dt_shmem
		if (!useSocket) {
			arg = args.get("name");
			arg.setValue(name);
		} else {
			// use port if using dt_socket
			arg = args.get("port");
			arg.setValue(port);
			arg = args.get("timeout");
			if(args != null) {
				arg.setValue("10");
			}
		}
		vm = connector.attach(args);

		// query capabilities
		if (!vm.canRedefineClasses()) {
			throw new Exception("JVM doesn't support class replacement");
		}
		console.println("Connected to:" + vm.description() + " (" + vm.version() + ")");
	}

	public void replace(File classFile, String className) throws Exception {
		// load class(es)
		byte[] classBytes = loadClassFile(classFile);
		// redefine in JVM
		List<ReferenceType> classes = vm.classesByName(className);

		// if the class isn't loaded on the VM, can't do the replace.
		if (classes == null || classes.size() == 0) {
			return;
		}

		for (int i = 0; i < classes.size(); i++) {
			ReferenceType refType = classes.get(i);
			HashMap<ReferenceType, byte[]> map = new HashMap<>();
			map.put(refType, classBytes);
			vm.redefineClasses(map);
		}
		// System.err.println("class replaced!");
	}

	public void disconnect() throws Exception {
		// nuthin to do here?
		vm.dispose();
	}

	public List<ReferenceType>  all(){
		return vm.allClasses();
	}
	
	private byte[] loadClassFile(File classFile) throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(classFile));

		byte[] ret = new byte[(int) classFile.length()];
		in.readFully(ret);
		in.close();

		// System.err.println("class file loaded.");
		return ret;
	}

}