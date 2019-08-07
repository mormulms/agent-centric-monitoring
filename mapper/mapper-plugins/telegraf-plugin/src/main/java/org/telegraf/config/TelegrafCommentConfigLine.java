package org.telegraf.config;

import java.util.LinkedList;

public class TelegrafCommentConfigLine extends TelegrafConfigLine {

	public static final String COMMENT_SPLIT = "##";
	public static final int COMMENT_SPLIT_LENGTH = 2;
	
	String content;
	
	public TelegrafCommentConfigLine(String line) {
		
		this.type = TelegrafConfigLineType.COMMENT;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();
		
		this.content = extractCommentFromString(processCommentsAndIndentationFromStringIntoList(line, commentsAndIndentations));
	}

	private String extractCommentFromString(String processedLine) {
		
		if(processedLine.startsWith(COMMENT_SPLIT)) {
			return processedLine.substring(COMMENT_SPLIT_LENGTH);
		} else {
			return null;
		}
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String toString() {
		
		return createCommentAndIndentationPrefix()+COMMENT_SPLIT+content;
	}

}
