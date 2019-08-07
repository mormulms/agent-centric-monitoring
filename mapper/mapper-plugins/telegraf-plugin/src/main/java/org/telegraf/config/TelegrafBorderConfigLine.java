package org.telegraf.config;

public class TelegrafBorderConfigLine extends TelegrafConfigLine {

	private static String content = "###############################################################################";
	
	public TelegrafBorderConfigLine() {
		
		this.type = TelegrafConfigLineType.BORDER;
		this.commentAndIndentationAllowed = false;
		
	}

	//TODO change to null?
	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String toString() {
		return content;
	}

}
