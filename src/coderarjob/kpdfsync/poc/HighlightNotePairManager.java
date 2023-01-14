package coderarjob.kpdfsync.poc;

import java.util.List;
import java.util.Collections;
import java.util.Hashtable;

public class HighlightNotePairManager
{
    private Hashtable<Integer, PageResource> mPageResources;

    public HighlightNotePairManager ()
    {
      mPageResources = new Hashtable<>();
    }

    /*** Checks if every note in every page has been paired with one highlight, in that page.
     * @Input: None
     * @Output: True, if there are no unpaired note left. False otherwise.
     */
    public boolean isComplete () throws Exception
    {
      for (PageResource res : mPageResources.values())
        if (res.isPairsComplete() == false)
          return false;

      return true;
    }

    /*** Creates pairs automatically for every page with exactly one Highlight and Note.
     * Automatic pairs can only be created when a page has either no notes, or has exactly one
     * highlight and one note. In cases where there are multiple notes and highlights, custom
     * explicit mapping need to be provided.
     * @Input: None
     * @Output: None
     */
    public void pairAutomatic () throws Exception
    {
      for (PageResource res : mPageResources.values()) {
        if (res.isValid())
          res.pairAutomatic();
      }
    }

    /*** Total number of highlights in every page.
     * @Input: None
     * @Output: Total number of highlights.
     */
    public int count () throws Exception
    {
      int totalCount = 0;
      for (PageResource res : mPageResources.values())
        totalCount += res.getPairs().size();

      return totalCount;
    }

    /*** List af all the page numbers, which has at least one highlight.
     * @Input: None
     * @Output: List of page numbers.
     */
    public List<Integer> getPageNumbers ()
    {
      return Collections.list (mPageResources.keys());
    }

    /*** PageReource object for the page number.
     * @Input: Page number
     * @Output: Corresponding page resource, containing highlight-notes pairs and unpaired notes.
     */
    public PageResource getPageResourceByPageNumber (int pageNumber)
    {
      return mPageResources.get (pageNumber);
    }

    /*** Adds unpaired note to the page resource for the corresponding page number.
     * @Input: Page number
     * @Input: Note text
     * @Output: None
     */
    public void addNote (int pageNumber, String text)
    {
      PageResource res = getPageResourceByPage (pageNumber);
      res.addNote (text);
    }

    /*** Adds a blank pair object with the highlight text in the page resource corresponding to the
     * page number.
     * @Input: Page number.
     * @Input: Highlight text
     * @Output: None
     */
    public void addHighlight (int pageNumber, String text)
    {
      PageResource res = getPageResourceByPage (pageNumber);
      res.addHighlight (text);
    }

    private PageResource getPageResourceByPage (int pageNumber)
    {
      PageResource pageRes = mPageResources.getOrDefault (pageNumber, null);
      if (pageRes == null)
      {
        pageRes = new PageResource (pageNumber);
        mPageResources.put (pageNumber, pageRes);
      }

      return pageRes;
    }
}
