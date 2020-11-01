package OpenNL.Protocols;

public abstract class Protocol {
	public abstract String toSocket(String s);
	public abstract String fromSocket(String s);
}
