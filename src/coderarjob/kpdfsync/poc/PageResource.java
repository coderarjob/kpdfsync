package coderarjob.kpdfsync.poc;

import java.util.List;
import java.util.ArrayList;

public class PageResource
{
  private final List<String> mNoteList;
  private final List<HighlightNotePair> mPairs;
  private final int mPageNumber;

  public PageResource (int pageNumber)
  {
    this.mNoteList = new ArrayList<>();
    this.mPairs = new ArrayList<>();
    this.mPageNumber = pageNumber;
  }

  /*** Page number associated with this page resource object.
   * @Input: None
   * @Output: None
   */
  public int getPageNumber ()
  {
    return this.mPageNumber;
  }

  /*** Returns an unmodifiable list of unpaired notes.
   * @Input: None
   * @Output: None
   */
  public List<String> getNoteList ()
  {
    // That way any changes do not effect object here.
    // List.copyOf added in JDK 10, thus cannot be used as we are targetting JDK 8.
    return copyOf(this.mNoteList);
  }

  /*** Returns an unmodifiable list of Highlights with already associated notes.
   * @Input: None
   * @Output: None
   */
  public List<HighlightNotePair> getPairs () throws Exception
  {
    if (isValid () == false)
      throw new Exception ("Invalid. There are more notes than highlights");

    // That way any changes do not effect object here.
    // List.copyOf added in JDK 10, thus cannot be used as we are targetting JDK 8.
    return copyOf(this.mPairs);
  }

  private <T> List<T> copyOf (List<T> list)
  {
    return new ArrayList<T>(list);
  }

  /*** Adds a new note to unpaired notes list.
   * No checks are done to see if same text was added before, as this is not an invalid scenario.
   * @Input: None
   * @Output: None
   */
  public void addNote (String note)
  {
    mNoteList.add (note);
  }

  /*** Adds a new highlight text.
   * No checks are done to see if same text was added before, as this is not an invalid scenario.
   * @Input: None
   * @Output: None
   */
  public void addHighlight (String highlight)
  {
    mPairs.add (new HighlightNotePair (highlight, null, mPageNumber));
  }

  /*** Creates pairs automatically for pages with exactly one Highlight and Note.
   * Automatic pairs can only be created when a page has either no notes, or has exactly one
   * highlight and one note. In cases where there are multiple notes and highlights, custom
   * explicit mapping need to be provided.
   * @Input: None
   * @Output: None
   */
  public boolean pairAutomatic () throws Exception
  {
    if (isValid () == false)
      throw new Exception ("Invalid. There are more notes than highlights");

    // Automatic mapping is only created in pages with only one note and highlight
    if (mPairs.size() != 1 || mNoteList.size() != 1)
      return false;

    // There is exactly one note and one highlight
    String highlight = mPairs.get (0).getHighlightText();
    String note = mNoteList.get (0);
    pair (highlight, note);
    return true;
  }

  /*** Disassociates a note from a highlight.
   * Finds the first highlight which matches the highlight text and has one note associated with
   * it. Note is then removed and put back into the unpaired notes list.
   * @Input: Highlight text which need to be unpaired.
   * @Output: None
   */
  public void unpair (String highlight) throws Exception
  {
    if (isValid () == false)
      throw new Exception ("Invalid. There are more notes than highlights");

    // Finds the first highlight which matches the highlight text and has one note associated with
    // it.
    int pairsListIndex = -1;
    for (int i = 0; i < mPairs.size(); i++)
    {
      HighlightNotePair pair = mPairs.get (i);
      if (pair.getHighlightText().equals (highlight) &&
          pair.getNoteText () != null)
        pairsListIndex = i;
    }

    if (pairsListIndex < 0)
      throw new Exception ("Unpair failed. Highlight text is not found or already unpaired.");

    // Remove the note from the pairs list and add it to the unpaired notes list.
    String note = mPairs.get (pairsListIndex).getNoteText();
    mPairs.get (pairsListIndex).setNoteText (null);

    mNoteList.add (note);
  }

  /*** Associates a note with another highlight.
   * Pair will fail after the first highlight which matches the highlight text has already been
   * assigned a note. There is no way handle multiple highlights with the same text properly.
   * @Input: Highlight to associate with.
   * @Input: Note to associate with.
   * @Output: None
   */
  public void pair (String highlight, String note) throws Exception
  {
    if (isValid () == false)
      throw new Exception ("Invalid. There are more notes than highlights");

    // Find highlight in the pairs list.
    int pairsListIndex = -1;
    for (int i = 0; i < mPairs.size(); i++)
      if (mPairs.get (i).getHighlightText().equals (highlight))
        pairsListIndex = i;

    if (pairsListIndex < 0)
      throw new Exception ("Pair failed. Highlight is not found.");

    // If there is already a note with the highlight, then we simply throw an exception.
    if (mPairs.get (pairsListIndex).getNoteText() != null)
      throw new Exception ("Pair failed. A note is already assigned.");

    // Find notes test in the unpaired notes list.
    int unpairedNotesListIndex = -1;
    for (int i = 0; i < mNoteList.size(); i++)
    {
      if (mNoteList.get(i).equals (note))
         unpairedNotesListIndex = i;
    }

    if (unpairedNotesListIndex < 0)
      throw new Exception ("Pair failed. Note is either already paired or is not found.");

    // Finally we pair.
    // Note was found in the unpaired notes list as well as the highlight is unpaired.
    mPairs.get (pairsListIndex).setNoteText (note);
    mNoteList.remove(unpairedNotesListIndex);
  }

  /*** Number of highlights must be more or equal to number of notes.
   * @Input: None
   * @Output: True if there are more or same number of highlights than notes.
   */
  public boolean isValid ()
  {
    int highlightCount, noteCount;
    highlightCount = this.mPairs.size();

    // Total number of notes.
    noteCount = this.mNoteList.size();
    for (HighlightNotePair pair : mPairs)
      noteCount += (pair.getNoteText() != null) ? 1 : 0;

    return (highlightCount >= noteCount);
  }

  /*** Checks if there is one highlight mapped to each of the notes in the page.
   * @Input: None
   * @Output: True, if there are no unpaired note left. False otherwise.
   */
  public boolean isPairsComplete () throws Exception
  {
    if (isValid () == false)
      throw new Exception ("Invalid. There are more notes than highlights");

    return mNoteList.size() == 0;
  }
}
