package org.helm.monomerservice;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class HELM2MonomerServiceApplication extends Application{
	
	private Set<Object> singletons = new HashSet<Object>();

	public HELM2MonomerServiceApplication() {
		singletons.add(new RestMonomer());
		singletons.add(new RestRule());
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}
