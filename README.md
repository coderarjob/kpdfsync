## About kpdfsync

![Screenshot](/docs/images/screenshot_alpha.png)

I sometimes read PDF documents on my Kindle and anyone who does the same, knows that highlights and
notes taken on the Kindle are not saved on the PDF file itself. This presents a huge problem
archiving my notes and to redo the highlights and copy the notes manually on to the PDF later on
is not practical for me.

This software tries to provide a solution. **kpdfsync** reads the ‘My Clippings.txt’ text file for
the highlights and notes and then applies them in the correct place on a PDF file.

Currently it is **under development**, so all the features may not work or be present.
There can also be serious bugs present, so I **request you to keep backup of the files you give to
kpdfsync (the clippings file and pdf files)**.

Here is the rough roadmap of the development.

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
- [X] **Alpha Release 1**

----

- [X] Bug Fixes
- [X] Feature: Fixing common PDF errors.
- [X] Usage guide for the end user.
- [X] **Alpha Release 2**

----

- [X] Bug Fixes: Some common PDF bugs are now fixed.
- [X] UI change to make it more suitable for screens with lower resolution.
- [X] **Alpha Release 3**

----

- [X] Bug Fixes: Some minor fixes in UI
- [X] Feature: Added new parser for reading older Kindle Clippings format.
- [X] UI change to select different parsers.
- [ ] **Alpha Release 4**

----

- [ ] Fine tune the rough edges in the supporting library.
- [ ] Memory/Resource optimization.
- [ ] Finalizing and optimizing the Graphical User Interface.
- [ ] **Beta Release**

----

## Installation

Once you have downloaded the [release binaries](https://github.com/coderarjob/kpdfsync/releases), and the minimum requirements are met,
to start Kpdfsync, execute the `kpdfsync` file in Linux/Mac OS or `kpdfsync.bat` in Windows.

### Minimum requirements
- JRE 1.8 __Note: JRE headless will not work.__
- Linux, Windows 7, Mac OS Sierra(\*)
- Linux: poppler-utils(\*\*)
- Windows: poppler-utils(\*\*) is included with the release. (will work out of the box)

(\*) PDF fixing feature does not work on Mac OS.
(\*\*) Required only for PDF fixing feature.

----

## Building from source

### Minimum requirements
- Linux, Windows 7, Mac OS Sierra
- JDK 8
- CMake 3.10

### Steps

Clone the repo or download the source. Then to build, run the following command at the top-level of
the source directory.

This will build jar files in `build/bin` directory, then run `build/bin/coderarjob.kpdfsync.jar`.

```
$ cmake -Bbuild
$ cd build
$ cmake --build . run
```

----

## Troubleshooting

* (Windows) Error: api-ms-win-crt-runtime-l1-1-0.dll is missing.
Install (Update for Universal C Runtime)[https://support.microsoft.com/en-us/kb/2999226]

## 3rd-party License

* PDF Clown library is used to read and highlight on PDF files. PDF Clown library is covered under
LGPL (GNU Lesser General Public License).
More information and source code can be found here [http://www.pdfclown.org](http://www.pdfclown.org)

* Poppler is covered under GPL3 license.
