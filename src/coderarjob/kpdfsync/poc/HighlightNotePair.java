package coderarjob.kpdfsync.poc;

public class HighlightNotePair
{
    private final String mHighlightText;
    private final int mPageNumber;
    private String mNoteText;

    public HighlightNotePair (String highlightText, String noteText, int pageNumber)
    {
        this.mHighlightText = highlightText;
        this.mNoteText = noteText;
        this.mPageNumber = pageNumber;
    }

    public String getHighlightText () { return this.mHighlightText; }
    public String getNoteText () { return this.mNoteText; }
    public void setNoteText (String value) { this.mNoteText = value; }
    public int getPageNumber () { return this.mPageNumber; }
}
