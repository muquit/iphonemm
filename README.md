<!-- TOC -->

- [History](#history)
- [Rules](#rules)

<!-- /TOC -->
# History
Mastermind game for iPhone. I wrote it when the first iPhone was released in 2007. The original page can be found in: [https://code.google.com/archive/p/iphonemm/](google code archive). The page is here for historical reasons only.

Mastermind game for iPhone. It should work with any web browsers with JavaScript on. But I wrote it mainly for iPhone, so I'll use the word iPhone instead of browser.

The game is written using Google Web Toolkit (GWT).

To play, point your iPhone to [http://muquit.com/iphonemm/](http://muquit.com/iphonemm/)

# Rules

* Your iPhone creates a code with 4 colors randomly picked from 6 colors (first row).
* The number of colors can repeat.
* Your goal is to find the code.
* Start by guessing 4 colors.
* Score will be given with black and/or white pegs.
* A black peg indicates a color is correct and it is also at the right position. A white peg indicates a color is correct but it is at the wrong position. The scoring pegs do not indicate the order of the guessed colors.
