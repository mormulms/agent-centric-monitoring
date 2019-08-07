package org.telegraf.config;

import java.util.List;

import lombok.Getter;

abstract public class TelegrafConfigLine {
	
	public static final String COMMENT_PREFIX = "# ";
	public static final String INDENTATION_PREFIX = "  ";
	public static final int PREFIX_LENGTH = 2;
	public static final int HEADER_WIDTH = 79;
	
	@Getter TelegrafConfigLineType type;
	
	@Getter boolean commentAndIndentationAllowed;
	@Getter List<String> commentsAndIndentations;
	
	
	public static TelegrafConfigLine fromString(String line) {
		switch (TelegrafConfigLineType.fromString(line)) {
		case DEFAULT:
			return new TelegrafDefaultConfigLine(line);
		case BORDER:
			return new TelegrafBorderConfigLine();
		case COMMENT:
			return new TelegrafCommentConfigLine(line);
		case HEADER:
			return new TelegrafHeaderConfigLine(line);
		case KEY_VALUE:
			return new TelegrafKeyValueConfigLine(line);
		case TABLE:	
			return new TelegrafTableConfigLine(line);
		
		default:
			return null;
		}
	}
	
	abstract public Object getContent();
	
	abstract public String toString();
	
	
	String processCommentsAndIndentationFromStringIntoList(String line, List<String> commentsAndIndentations) {
		//TODO special case single # in line
		if (line.length()==1 && line.startsWith("#")) {
			commentsAndIndentations.add(COMMENT_PREFIX);
			return new String();
		}
		
		while (line.startsWith(COMMENT_PREFIX) || line.startsWith(INDENTATION_PREFIX)) {
			if (line.startsWith(COMMENT_PREFIX)) {
				commentsAndIndentations.add(COMMENT_PREFIX);
			} else {
				commentsAndIndentations.add(INDENTATION_PREFIX);
			}
			line = line.substring(PREFIX_LENGTH);
		}
		
		return line;
	}
	
	String createCommentAndIndentationPrefix() {
		StringBuilder builder = new StringBuilder();
		commentsAndIndentations.stream().forEach(prefix -> builder.append(prefix));
		
		return builder.toString();
	}
		
	public void commentWholeLine() {
		if(commentAndIndentationAllowed) {
			commentsAndIndentations.add(0,COMMENT_PREFIX);
		}
	}
	
	public void uncommenWholeLine() {
		if(commentAndIndentationAllowed) {
			if(!commentsAndIndentations.isEmpty() && commentsAndIndentations.get(0).equals(COMMENT_PREFIX)) {
				commentsAndIndentations.remove(0);
			}
		}
	}
	
	public void commentContent() {
		if(commentAndIndentationAllowed) {
			commentsAndIndentations.add(COMMENT_PREFIX);
		}
	}
	
	public void uncommentContent() {
		if(commentAndIndentationAllowed) {
			if(!commentsAndIndentations.isEmpty() && commentsAndIndentations.get(commentsAndIndentations.size()-1).equals(COMMENT_PREFIX)) {
				commentsAndIndentations.remove(commentsAndIndentations.size()-1);
			}
		}
	}
		
	public void indentContent() {
		if (commentAndIndentationAllowed) {
			commentsAndIndentations.add(INDENTATION_PREFIX);
		}
	}

	public static TelegrafConfigLine createEmptyLine() {
		return new TelegrafDefaultConfigLine(new String());
	}
	
	
}
