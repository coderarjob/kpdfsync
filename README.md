## About kpdfsync

![Screenshot](/docs/images/screenshot_alpha.png)

If you use Kindle to read PDF books or documents, you might have seen that the highlights and notes
made on the Kindle are not saved on the PDF file itself. This means, that if you take the PDF file
from your Kindle and read on another device, you will not see those highlights and notes there.

This software tries to provide a solution. The basis is the Clippings.txt file on your Kindle.
Kindle saves the page numbers and content of the highlights and notes in the text file. So in
theory, one can read the Clippings file and reapply the highlights and notes on the PDF separetely.
This software automates the process.

Currently it is in development, so not all the features work or even present. Here is the rough
roadmap.

## Roadmap

- [X] Parsing the Clippings.txt file
- [X] Search for the highlighted text in a page of the PDF file.
- [X] Annotate highlight and notes on the PDF file.
- [X] Graphical User Interface (GUI) testing.
- [X] Highlights to notes mapping. This is required, because the clippings text file does not
  provide information which can used to determine which notes are related to which highlight on a
  single page. Some cases where a page contains a single note and highlight, automatic pairs are
  created, however in cases where there are more than one note, these associations can be created
  manually by the user.
- [X] GUI finalizing for the Alpha release.
- [X] Debug loggings
- [ ] **Alpha Release**

----

- [ ] Fine tune the rough edges in the supporting library.
- [ ] Memory/Resource optimization.
- [ ] Finalizing and optimizing the Graphical User Interface.
- [ ] **Beta Release**

## Requirements
- JRE 1.8 or higher
- Linux, Mac, Windows

## 3rd-party License

* PDF Clown library is used to read and highlight on PDF files. PDF Clown library is covered under
LGPL (GNU Lesser General Public License).
More information and source code can be found here [http://www.pdfclown.org](http://www.pdfclown.org)
