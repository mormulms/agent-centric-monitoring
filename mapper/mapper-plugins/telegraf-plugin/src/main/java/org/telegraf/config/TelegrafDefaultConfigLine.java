package org.telegraf.config;

import java.util.LinkedList;

public class TelegrafDefaultConfigLine extends TelegrafConfigLine {
	
	String content;
	
	//TODO change
	private boolean isSingleNumberSign;
	
	public TelegrafDefaultConfigLine(String line) {
		
		this.type = TelegrafConfigLineType.DEFAULT;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();
		
		//TODO change
		this.isSingleNumberSign = line.length()==1 && line.startsWith("#");
		this.content = processCommentsAndIndentationFromStringIntoList(line, commentsAndIndentations);
	}

	@Override
	public Object getContent() {

		return content;
	}

	@Override
	public String toString() {
		if (isSingleNumberSign) {
			return "#";
		}
		return createCommentAndIndentationPrefix()+content;
	}
}
