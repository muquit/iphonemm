package com.muquit.client;

/*
** MasterMind game for iPhone by muquit@muquit.com
**
** It should work with any browsers that support JavaScripts.
** But it's written keeping iPhone in mind, meaning it will
** not look as pretty as it does on iPhone.
**
** Please send bug reports, suggestions etc. to muquit@gmail.com
** 
** This is a free software covered by the GNU General Public License v2
** (http://www.gnu.org/licenses/gpl2.txt)
**
** Enjoy!
**
** http://www.muquit.com/muquit/software/
*/

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class iPhoneMM implements EntryPoint
{
    final String VERSION_S="1.01";

    MyImageBundle gMyImageBundle= (MyImageBundle)GWT.create(MyImageBundle.class);
    
    final String scoreStrip="./images/score_strip.png";
    final String scoreBlackPeg="./images/score_black.png";
    final String scoreWhitePeg="./images/score_white.png";
    
    final int gStartx=2;
    final int gStarty=2;
    
    /* order is important here. do not mess with it */
    final int RED_BALL     = 0;
    final int GREEN_BALL   = 1;
    final int BLUE_BALL    = 2;
    final int CYAN_BALL    = 3;
    final int MAGENTA_BALL = 4;
    final int YELLOW_BALL  = 5;
    final int CODE_BALL    = 6;
    final int PLAY_BALL    = 7;
    final Image[] gBallImage=new Image[PLAY_BALL + 1];
    
    final int MAX_COLORS    = 6;
    final int NUM_COLS      = 4; 
    final int NUM_ROWS      = 9; 
    final int BALL_WIDTH    = 30;
    final int BALL_HEIGHT   = 30;
    
    final int BALL_TYPE_COLOR_BALLS = 1;
    final int BALL_TYPE_PLAY_PEGS   = 2;
    
    final int RIGHT_COLOR_RIGHT_POS = 1;
    final int RIGHT_COLOR_WRONG_POS = 2;
    final int WRONG_COLOR_WRONG_POS = 3;
    
    /* iPhone screen size is 320x480. we use 320x(480-60) pixels. */
    final String SCREEN_WIDTH="320px";
    final String SCREEN_HEIGHT="480px";
    
    /* gap between balls */
    final int gHgap=(320 - 2 * 2  - 6 * 30) / 5;
    
    final Image gStripImage=new Image(scoreStrip);
    final Image[] gPlayPegImage=new Image[NUM_ROWS * NUM_COLS];
    final Image[] scorePegImage=new Image[NUM_ROWS * NUM_COLS];
    
    final int[] gRowY=new int[NUM_ROWS];
    final boolean REVEAL_CODE=true;
    final boolean FIRST_TIME=true;
    
    /* AbsolutePanel for positioning everything */
    final AbsolutePanel gAp=new AbsolutePanel();
    MyDialog gRulesDialog=null;
    
    int[] gBallX={0,0,0,0,0,0};
    int[] gScorePegsXY=new int[NUM_ROWS * (NUM_COLS*2) + 1];
    int[] gDirtyPlayPegsX=new int[NUM_ROWS * NUM_COLS];
    int[] gDirtyPlayPegsY=new int[NUM_ROWS * NUM_COLS];
    
    int[] gCodePeg={-1,-1,-1,-1};
    int[] gGuessPeg={-1,-1,-1,-1};
    
    boolean gImageListenerInstalled=false;
    
    int gLastSelected=0;
    int gNowSelected=0;
    int gRowCount=0;
    int gCurrentRowY=0;
    int gScoreColIdx=0;
    int gNdirtyRows=0;
    
    public void onModuleLoad()
    {
        createImagesFromImageBundle();
        createAbsolutePanel();
        createPlayImages();
        drawBoard(FIRST_TIME);
    }
    
   /**
    * Create a AbsolutePanel and attach it to RootPanel
    */
    private void createAbsolutePanel()
    {
        RootPanel rootPanel=RootPanel.get();
        gAp.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);
        rootPanel.add(gAp,0,0);
    }
    
   /**
    * create the gray ball images
    */
    private void createPlayImages()
    {
        for (int i=0; i < NUM_ROWS * NUM_COLS; i++)
        {
            gPlayPegImage[i]=makeNewImage(PLAY_BALL);
        }
    }
    
   /**
    * create images from ImageBundle. 6 colored balls, the code ball 
    * (question mark on it) and the gray ball (play are) are in a ImageBundle
    */
    private void createImagesFromImageBundle()
    {
        gBallImage[RED_BALL]=gMyImageBundle.openRedBall().createImage();
        gBallImage[GREEN_BALL]=gMyImageBundle.openGreenBall().createImage();
        gBallImage[BLUE_BALL]=gMyImageBundle.openBlueBall().createImage();
        gBallImage[CYAN_BALL]=gMyImageBundle.openCyanBall().createImage();
        gBallImage[MAGENTA_BALL]=gMyImageBundle.openMagentaBall().createImage();
        gBallImage[YELLOW_BALL]=gMyImageBundle.openYellowBall().createImage();
        gBallImage[CODE_BALL]=gMyImageBundle.openCodeBall().createImage();
        gBallImage[PLAY_BALL]=gMyImageBundle.openPlayBall().createImage();
        
        /* pull the black and white peg image so they'll be cached */
        Image blah=new Image();  blah.setUrl(scoreBlackPeg);
        Image blah2=new Image(); blah2.setUrl(scoreWhitePeg);
    }
    
    private void resetDirtyPegs()
    {
        for (int i=0; i < NUM_ROWS * NUM_COLS; i++)
        {
            gDirtyPlayPegsX[i]=-1;
            gDirtyPlayPegsY[i]=-1;
        }
        gNdirtyRows=0;
    }
    
   /**
    * draw board. carefully reset some variables.
    */
    private void drawBoard(boolean firstTime)
    {
        
        gNowSelected=0;
        gScoreColIdx=0;
        gCurrentRowY=0;
        
        makeCode();
        
        if (firstTime)
        {
            resetDirtyPegs();
        }
        drawColorBalls(firstTime);
        drawCodeBalls(!REVEAL_CODE);
        
        if (firstTime)
        {
            drawRulesButton();
            drawResetButton();
        }
        //long sTime=System.currentTimeMillis();
        
        drawPlayPegs(firstTime);
        gCurrentRowY=gRowY[0]; // y of first ball
        
        //long eTime=System.currentTimeMillis();
        //long elapsed=eTime - sTime;
        //myDebug("drawPlayPegs took: " + elapsed);
        //sTime=System.currentTimeMillis();
        
        drawScoringPegs(firstTime);
        gRowCount=0;
        
        //eTime=System.currentTimeMillis();
        //elapsed=eTime - sTime;
        //myDebug("drawScorePegs took: " + elapsed);
        
        resetDirtyPegs();
        gImageListenerInstalled=true;
    }
    
    private void drawRulesButton()
    {
        int x=6 + 4 * BALL_WIDTH + 3 * gHgap;
        int y=BALL_HEIGHT + 14;
        final Button rulesButton=new Button();
        gAp.add(rulesButton,x,y);
        rulesButton.setText("Rules");
        rulesButton.addClickListener(new ClickListener() {
            public void onClick(final Widget sender)
            {
                if (gRulesDialog == null)
                {
                    String rulesMsg=
" Your iPhone makes a secret code with four colors randomly picked" +
" from six colors. The same color can repeat." +
" Your goal is to find the code. Start by guessing four colors." +
" Score will be given for your guess with black and white pegs." +
"<p>" +
"<b>black peg: right color and right position.</b><br>" +
"<b>white peg: right color but wrong position.</b>" +
"  The scoring pegs do not indicate the order of the guessed colors." +
"<p>" + 
" <a href=\"http://www.muquit.com/muquit/software/\">http://www.muquit.com/</a>" +
"<p>" +
"Have Fun!<br>";
                    
                    gRulesDialog=new MyDialog(rulesMsg,"MasterMind v" + VERSION_S + " for iPhone");
                    gRulesDialog.setPixelSize(310,330);
                    gRulesDialog.setPopupPosition(2,60);
                    gRulesDialog.show();
                }
                else
                {
                    gRulesDialog.show();
                }
            }
        });
    }
    
    private void drawResetButton()
    {
        int x=6 + 4 * BALL_WIDTH + 3 * gHgap + 5;
        int y=BALL_HEIGHT + 14;
        final Button resetButton=new Button();
        x=x+50;
        gAp.add(resetButton,x,y);
        resetButton.setText("Reset");
        resetButton.addClickListener(new ClickListener() {
            public void onClick(final Widget sender)
            {
                drawBoard(!FIRST_TIME);
            }
        });
    }
    
   /**
    * draw the colored balls at the top
    */
    private void drawColorBalls(boolean firstTime)
    {
        if (!firstTime) /* resetting */
        {
            /* just select the first all. no need to redraw */
            refreshBallBorder();
            gLastSelected=0;
            return;
        }
        /*
        if (!gImageListenerInstalled)
        {
            myDebug("drawColorBalls(): image listener will be installed");
        }
        else
        {
            myDebug("drawColorBalls(): image listener already installed");
        }
        */
        
        int x=0;
        int y=gStarty;
        for (int i=0; i < MAX_COLORS; i++)
        {
            if (i == 0)
                x=gStartx;
            else
                x=gStartx + i * BALL_WIDTH + i * gHgap;
            
            /* draw the ball */
            gAp.add(gBallImage[i],x,y);
            /* store the x coordinate */
            gBallX[i]=x;
            if (!gImageListenerInstalled)
            {
                MyImageClickListener imgListener=new MyImageClickListener(x,y);
                imgListener.setImageIdx(i);
                imgListener.setBallType(BALL_TYPE_COLOR_BALLS);
                gBallImage[i].addClickListener(imgListener);
            }
        }
        refreshBallBorder();
    }
    
   /**
    * draw the 4 question balls. code will be revealed there
    * @param showCodes  if true reveal the code. otherwise draw the ? balls
    */
    private void drawCodeBalls(boolean showCodes)
    {
        int x=0;
        int y=BALL_WIDTH + 2 + 12;;
        for (int i=0; i < NUM_COLS; i++)
        {
            if (i == 0)
                x=gStartx;
            else
               x=gStartx + i * BALL_WIDTH + i * gHgap;
            
            //myDebug("Code: x,y: " + x + "," + y + " gap: " + gHgap);
            
            if (! showCodes)
            {
                gAp.add(makeNewImage(CODE_BALL),x,y);
            }
            else
            {
                gAp.add(makeNewImage(gCodePeg[i]),x,y);
            }
        }
        if (showCodes)
        {
            /*
             * we revealed the code. set row count to last row. this will 
             * prevent processing any more selection.
             */
            gRowCount=NUM_ROWS;
        }
    }     
    
    /* make a new image from the bundle */
    private Image makeNewImage(int ballNum)
    {
        Image ballImage=null;
        switch(ballNum)
        {
            case RED_BALL: /* red */
            {
                ballImage=gMyImageBundle.openRedBall().createImage();
                break;
            }
            case GREEN_BALL: /* green */
            {
                ballImage=gMyImageBundle.openGreenBall().createImage();
                break;
            }
            case BLUE_BALL: /* blue */
            {
                ballImage=gMyImageBundle.openBlueBall().createImage();
                break;
            }
            
            case CYAN_BALL: /* cyan */
            {
                ballImage=gMyImageBundle.openCyanBall().createImage();
                break;
            }
            
            case MAGENTA_BALL: /* magenta */
            {
                ballImage=gMyImageBundle.openMagentaBall().createImage();
                break;
            }
            
            case YELLOW_BALL: /* yellow */
            {
                ballImage=gMyImageBundle.openYellowBall().createImage();
                break;
            }
            case CODE_BALL: /* code ball */
            {
                ballImage=gMyImageBundle.openCodeBall().createImage();
                break;
            }

            case PLAY_BALL: /* gray circular one */
            {
                ballImage=gMyImageBundle.openPlayBall().createImage();
                break;
            }
        }
        return(ballImage);
    }
    
    /**
     * make code. Generate 4 random numbers ranging 0 to MAX_COLORS-1
     */
    private void makeCode()
    {
        for (int col=0; col < NUM_COLS; col++)
        {
            gCodePeg[col]=Random.nextInt(MAX_COLORS);
            //myDebug("CodeX: " + gCodePeg[col]);
            //gCodePeg[col]=(int) Math.floor(Math.random() * MAX_COLORS);
            //myDebug("CodeY: " + gCodePeg[col]);
        }
    }
    
    private void drawPlayPegs(boolean firstTime)
    {
        /*
        if (!gImageListenerInstalled)
        {
            myDebug("drawPlayPegs(): image listener will be installed");
        }
        else
        {
            myDebug("drawPlayPegs(): image listener already installed");
        }
        */
        
        int col=0;
        int row=0;
        if (gNdirtyRows > 0)
        {
            int dirtyPegs=gNdirtyRows * NUM_COLS;
            //myDebug("Dirty Pegs: " + dirtyPegs);
            
            row=0;
            col=0;
            /* just draw the changed cells */
            for (int peg=0; peg < dirtyPegs; peg++)
            {
                if (col >= NUM_COLS)
                {
                    row++;
                    col=0;
                }
                if (gDirtyPlayPegsX[peg] != -1)
                {
                    //myDebug("Just Redraw: peg: " + peg + " col: " + col + " row: " + row + " x,y: " +  gDirtyPlayPegsX[peg] + "," + gDirtyPlayPegsY[peg]);
                    Image pegImage=gPlayPegImage[peg];
                    redrawPeg(pegImage,gDirtyPlayPegsX[peg],gDirtyPlayPegsY[peg],row,col);
                }
                col++;
            }
            return;
        }
        if (gNdirtyRows == 0 && !firstTime)
        {
            /* we're resetting and nothing has changed */
            //myDebug("Reset draw play pegs. nothing changed.");
            return;
        }
        
        int ic=0;
        int yy=82;
        int gap=57; 
        int max=gap * NUM_COLS;
        int vSpacer=36;
        for (row=0; row < NUM_ROWS; row++)
        {
            col=0;
            gRowY[row]=yy;
            for (int xx=gStartx; xx <= max; xx += gap)
            {
                Image img=gPlayPegImage[ic++];
                redrawPeg(img,xx,yy,row,col);
                col++;
            }
            yy += vSpacer;
        }
    }
    
   /**
    * draw the image and also install a new ClickListener
    */
    private void redrawPeg(Image pegImage,int left,int top,int row,int col)
    {
        gAp.add(pegImage,left,top);
        if (!gImageListenerInstalled)
        {
            MyImageClickListener imgListener=new MyImageClickListener(left,top);
            pegImage.addClickListener(imgListener);
            int idx=NUM_COLS * row + col;
            imgListener.setImageIdx(idx);
            imgListener.setBallType(BALL_TYPE_PLAY_PEGS);
            imgListener.setClickedCol(col);
        }
            
    }
    
    private void drawScoringPegs(boolean firstTime)
    {
        /*
        ** drawing individual pegs are extremely slow. So all the scoring
        ** pegs are in a strip of image. 
        */
        
        int left=211;
        int top=87;
        if (gRowCount > 0)
        {
            //myDebug("drawing Scoring scoring strip again");
            gAp.add(gStripImage,left,top);
            return;
        }
        if (!firstTime)
        {
            return;
        }
        
        //myDebug("drawing Scoring scoring strip");
        
        gAp.add(gStripImage,left,top);
        
        int idx=0;
        int yy=top;
        for (int row=0; row < NUM_ROWS; row++)
        {
            for (int xx=211; xx <= 298; xx += 29)
            {
                gScorePegsXY[idx]=xx;
                gScorePegsXY[idx+1]=yy;
                idx += 2;
            }
            yy += 37;
        }
    }
    
    private void scoreTheRow()
    {
        int[] guessPos={0,0,0,0};
        int[] codePos={0,0,0,0};
        int rcRp=0;
        int rcWp=0;
       
        //myDebug("In scoreTheRow: row=" + gRowCount);
        if (gRowCount >= NUM_ROWS)
        {
            return;
        }
        
        int col;
        for (col=0; col < NUM_COLS; col++)
        {
            guessPos[col]=codePos[col]=WRONG_COLOR_WRONG_POS;
        }
        for (col=0; col < NUM_COLS; col++)
        {
            if (gGuessPeg[col] == gCodePeg[col])
            {
                guessPos[col]=codePos[col]=RIGHT_COLOR_RIGHT_POS;
                rcRp++;
            }
        }
        if (rcRp != NUM_COLS)
        {
            for (int i=0; i < NUM_COLS; i++)
            {
                for (int j=0; j < NUM_COLS; j++)
                {
                    if (gGuessPeg[i] == gCodePeg[j]         &&
                        codePos[j] == WRONG_COLOR_WRONG_POS &&
                        guessPos[i] == WRONG_COLOR_WRONG_POS)
                    {
                        guessPos[i]=codePos[j]=RIGHT_COLOR_WRONG_POS;
                        rcWp++;
                    }
                }
            }
        }
        int x,y;
        for (int i=0; i < rcRp; i++)
        {
            x=gScorePegsXY[gScoreColIdx];
            y=gScorePegsXY[gScoreColIdx+1];
            gScoreColIdx += 2;
            /* score with black pegs */
            gAp.add(new Image(scoreBlackPeg),x,y);
        }
        for (int i=0; i < rcWp; i++)
        {
            x=gScorePegsXY[gScoreColIdx];
            y=gScorePegsXY[gScoreColIdx+1];
            gScoreColIdx += 2;
            /* score with white pegs */
            gAp.add(new Image(scoreWhitePeg),x,y);
        }
        if ((rcRp + rcWp) < NUM_COLS)
        {
            int left=NUM_COLS - rcRp - rcWp;
            gScoreColIdx += (left * 2);
        }
        if (rcRp == NUM_COLS)
        {
            //myDebug("You found the code!" + guessPos[0] + guessPos[1] + guessPos[2] + guessPos[3]);
            gRowCount++;
            String msg="You found the code in " + gRowCount + " moves!";
            MyDialog myDialog=new MyDialog(msg,"Congtratulations!");
            myDialog.setPopupPosition(30,82);
            myDialog.show();
            gCurrentRowY=gRowY[0];
            drawCodeBalls(true);
        }
        else if (gRowCount == (NUM_ROWS - 1))
        {
            drawCodeBalls(true);
            String msg="Try Again. Better luck next time!";
            MyDialog myDialog=new MyDialog(msg,"Sorry!");
            myDialog.setPopupPosition(30,82);
            myDialog.show();
        }
        //myDebug("Rowcount: " + gRowCount);
        gRowCount++;
        if (gRowCount <= NUM_ROWS)
            gCurrentRowY=gRowY[gRowCount];
        else
            gCurrentRowY=gRowY[0];
        gGuessPeg[0]=gGuessPeg[1]=gGuessPeg[2]=gGuessPeg[3]=-1;
    }
    
    private void myDebug(String msg)
    {
        System.out.println(msg);
    }
   
   /**
    * when a colored ball is clicked to select it, put a border 
    * around it and undo border around the last selected one.
    */
    private void refreshBallBorder()
    {
        //myDebug(" Now selected: " + gNowSelected);
        //myDebug("Last selected: " + gLastSelected);
        Image lastBall=gBallImage[gLastSelected];
        Image currentBall=gBallImage[gNowSelected];
        currentBall.setStyleName("colored-Balls");
        DOM.setStyleAttribute(lastBall.getElement(),"borderStyle","none");
        //String borderColor=DOM.getStyleAttribute(currentBall.getElement(),"borderColor");
        //myDebug("Border color: " + borderColor);
        DOM.setStyleAttribute(currentBall.getElement(),"borderColor","#000000");
        DOM.setStyleAttribute(currentBall.getElement(),"borderStyle","solid");
        DOM.setStyleAttribute(currentBall.getElement(),"borderWidth","1px");
    }
    
   /**
    * our very own ClickListener for the images. All clicks are handled
    * here.
    */
    private class MyImageClickListener implements ClickListener
    {
        private int imgX,imgY;
        private int imgIdx;
        private int clickedRow;
        private int clickedCol;
        private int ballType=-1;
        
        public MyImageClickListener(int x,int y)
        {
            imgX=x;
            imgY=y;
        }
        
        MyImageClickListener(int idx)
        {
            imgIdx=idx;
        }
        
        public void setImageIdx(int idx)
        {
            imgIdx=idx;
        }
        public int getImageIdx()
        {
            return(imgIdx);
        }
        
        public void setBallType(int type)
        {
            ballType=type;
        }
        
        public int getBallType(int type)
        {
            return(ballType);
        }
        
        public void setClickedCol(int col)
        {
            clickedCol=col;
        }
        
        public int getClickedCol()
        {
            return(clickedCol);
        }
        
        public void onClick(Widget sender)
        {
            if (gRowCount >= NUM_ROWS)
            {
                //myDebug("Last row reached.. returning");
                return;
            }
            clickedRow=imgY;
            
            clickedRow=imgY;
            
            if (ballType == BALL_TYPE_COLOR_BALLS)
            {
                int ball=0;
                /* find out which ball is clicked */
                for (ball=0; ball < MAX_COLORS; ball++)
                {
                    if (imgX == gBallX[ball])
                    {
                        break;
                    }
                }
                gNowSelected=ball;
                refreshBallBorder();
                if (gLastSelected == gNowSelected)
                {
                    return;
                }
                /* set border around current ball. remove the same from the last ball */
                gLastSelected=ball;
                return;
            }
            
            if (ballType == BALL_TYPE_PLAY_PEGS)
            {
                if (clickedRow != gCurrentRowY)
                {
                    //myDebug("returning.." + clickedRow + "," + gCurrentRowY);
                    return;
                }
                
                if (gGuessPeg[0] != -1 && 
                    gGuessPeg[1] != -1 && 
                    gGuessPeg[2] != -1 && 
                    gGuessPeg[3] != -1)
                {
                    scoreTheRow();
                    return;
                }
                int idx=NUM_COLS * gRowCount + clickedCol;
                gDirtyPlayPegsX[idx]=imgX;
                gDirtyPlayPegsY[idx]=imgY;
                gNdirtyRows=gRowCount + 1;
                
                Image nImage=makeNewImage(gLastSelected);
                gAp.add(nImage,imgX,imgY);
                
                /* install a new click listener otherwise color can not be changed */
                MyImageClickListener listener=new MyImageClickListener(imgX,imgY);
                nImage.addClickListener(listener);
                listener.setImageIdx(idx);
                listener.setBallType(BALL_TYPE_PLAY_PEGS);
                listener.setClickedCol(clickedCol);
                
                //myDebug(" Saving: cc: " + clickedCol + "  idx: " + idx + " " + imgX + "," + imgY);
               
                gGuessPeg[clickedCol]=gLastSelected;
                
                if (gGuessPeg[0] != -1 && 
                    gGuessPeg[1] != -1 && 
                    gGuessPeg[2] != -1 && 
                    gGuessPeg[3] != -1)
                {
                    scoreTheRow();
                    return;
                }
            }
        }
    }
    
    private class MyDialog extends DialogBox implements ClickListener
    {
        public MyDialog(String msg,String title)
        {
            setText(title);
            
            /* create a DockPanel*/
            DockPanel dock=new DockPanel();
            Button closeButton=new Button("Close",this);
            
            /* create a HTML widget */
            HTML htmlMsg=new HTML(msg,true);
            
            /* add HTML widget to panel */
            dock.add(htmlMsg,DockPanel.NORTH);
            dock.setCellVerticalAlignment(htmlMsg,DockPanel.ALIGN_TOP);
            
            /* add the button to panel */
            dock.add(closeButton,DockPanel.SOUTH);
            dock.setCellHorizontalAlignment(closeButton,DockPanel.ALIGN_CENTER);
            
            setWidget(dock);
        }
        
        public void onClick(Widget sender)
        {
            hide();
        }
        /*
        public void onMouseDown(Widget sender,int x,int y)
        {
            myDebug("x,y: " + x + "," + y);
            hide();
        }
        */
    }
    
   /**
    * we can gain performance by using ImageBundle for the 7 same sized balls. It'll
    * take only 1 HTTP request to get them all and byte count becomes slightly
    * smaller as well. We'll keep the 2 15x15 balls and the strip image out
    * of the bundle.
    */
    public interface MyImageBundle extends ImageBundle
    {
       /**
        * @gwt.resource com/muquit/client/images/ball-red.png
        */
        AbstractImagePrototype openRedBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-green.png
        */
        AbstractImagePrototype openGreenBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-blue.png
        */
        AbstractImagePrototype openBlueBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-cyan.png
        */
        AbstractImagePrototype openCyanBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-magenta.png
        */
        AbstractImagePrototype openMagentaBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-yellow.png
        */
        AbstractImagePrototype openYellowBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-code.png
        */
        AbstractImagePrototype openCodeBall();
        
       /**
        * @gwt.resource com/muquit/client/images/ball-play.png
        */
        AbstractImagePrototype openPlayBall();
    }
}
