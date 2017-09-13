package com.rewedigital.examples.msintegration.composer.composing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.attoparser.AbstractMarkupHandler;
import org.attoparser.ParseException;
import org.attoparser.util.TextUtil;

public class ContentMarkupHandler extends AbstractMarkupHandler {

    private int contentStart = 0;
    private int contentEnd = 0;

    private final List<String> links = new LinkedList<>();

    private boolean parsingHead = false;
    private boolean parsingLink = false;

    private Map<String, String> attributes;
    private ContentRange defaultContentRange;

    public List<String> assetLinks() {
        return links;
    }

    public ContentMarkupHandler(final ContentRange defaultContentRange) {
        this.defaultContentRange = defaultContentRange;
    }

    public ContentRange contentRange() {
        return contentEnd <= 0 ? defaultContentRange : new ContentRange(contentStart, contentEnd);
    }

    @Override
    public void handleStandaloneElementStart(final char[] buffer, final int nameOffset, final int nameLen,
        final boolean minimized, final int line,
        final int col) throws ParseException {
        super.handleStandaloneElementStart(buffer, nameOffset, nameLen, minimized, line, col);
        if (parsingLink) {
            // next element after standalone element
            pushLink();
        }

        if (parsingHead && isLinkElement(buffer, nameOffset, nameLen)) {
            startLink();
        }
    }

    @Override
    public void handleOpenElementStart(final char[] buffer, final int nameOffset, final int nameLen, final int line,
        final int col) throws ParseException {
        super.handleOpenElementStart(buffer, nameOffset, nameLen, line, col);
        if (parsingLink) {
            // next element after standalone element
            pushLink();
        }

        if (isHeadElement(buffer, nameOffset, nameLen)) {
            parsingHead = true;
        } else if (parsingHead && isLinkElement(buffer, nameOffset, nameLen)) {
            startLink();
        } else if (isContentElement(buffer, nameOffset, nameLen)) {
            contentStart = nameOffset + nameLen + 1;
        }
    }

    @Override
    public void handleCloseElementEnd(final char[] buffer, final int nameOffset, final int nameLen, final int line,
        final int col) throws ParseException {
        super.handleCloseElementEnd(buffer, nameOffset, nameLen, line, col);
        if (isHeadElement(buffer, nameOffset, nameLen)) {
            parsingHead = false;
        } else if (isLinkElement(buffer, nameOffset, nameLen)) {
            pushLink();
        } else if (isContentElement(buffer, nameOffset, nameLen) && contentStart >= 0) {
            contentEnd = nameOffset - 2;
        }
    }

    @Override
    public void handleAttribute(char[] buffer, int nameOffset, int nameLen, int nameLine, int nameCol,
        int operatorOffset, int operatorLen, int operatorLine, int operatorCol, int valueContentOffset,
        int valueContentLen, int valueOuterOffset, int valueOuterLen, int valueLine, int valueCol)
        throws ParseException {
        super.handleAttribute(buffer, nameOffset, nameLen, nameLine, nameCol, operatorOffset, operatorLen, operatorLine,
            operatorCol, valueContentOffset, valueContentLen, valueOuterOffset, valueOuterLen, valueLine, valueCol);

        if (parsingLink) {
            attributes.put(new String(buffer, nameOffset, nameLen),
                new String(buffer, valueContentOffset, valueContentLen));
        }
    }

    private boolean isLinkElement(final char[] buffer, final int nameOffset, final int nameLen) {
        return TextUtil.contains(true, buffer, nameOffset, nameLen, "link", 0, "link".length());
    }

    private boolean isHeadElement(final char[] buffer, final int nameOffset, final int nameLen) {
        return TextUtil.contains(true, buffer, nameOffset, nameLen, "head", 0, "head".length());
    }


    private boolean isContentElement(final char[] buffer, final int nameOffset, final int nameLen) {
        return contentEnd <= 0
            && TextUtil.contains(true, buffer, nameOffset, nameLen, "rewe-digital-content", 0,
                "rewe-digital-content".length());
    }


    private void startLink() {
        parsingLink = true;
        attributes = new HashMap<>();
    }

    private void pushLink() {
        if (attributes.getOrDefault("data-rd-options", "").contains("include")) {
            links.add(
                attributes
                    .entrySet()
                    .stream()
                    .reduce(new StringBuilder("<link "),
                        (builder, e) -> builder.append(e.getKey())
                            .append("=\"")
                            .append(e.getValue())
                            .append("\" "),
                        (a, b) -> a.append(b))
                    .append("/>").toString());
        }
        parsingLink = false;
        attributes = null;
    }



}
