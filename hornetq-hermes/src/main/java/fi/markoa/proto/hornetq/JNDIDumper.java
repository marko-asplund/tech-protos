package fi.markoa.proto.hornetq;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class JNDIDumper {

	public static void main(String... args) throws NamingException {
		Context ctx = new InitialContext();

		System.out.println("Listing root JNDI context:");
		NamingEnumeration<NameClassPair> list = ctx.list("");
		if (list.hasMore()) {
			while (list.hasMore()) {
				NameClassPair ncp = list.next();
				System.out.println(ncp.getName() + " (" + ncp.getClassName() + ")");
			}
		} else {
			System.out.println("Empty list!");
		}

		System.out.println(ctx.lookup("java:jms/RemoteConnectionFactory").getClass().getName());
		
		System.out.println(ctx.lookup("java:jms/queue/HELLOWORLDMDBQueue").getClass().getName());
//		System.out.println(ctx.lookup("java:ConnectionFactory").getClass().getName());

	}

}
