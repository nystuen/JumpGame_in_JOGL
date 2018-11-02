
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import javax.swing.*;

import java.util.Random;
import java.util.Random.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;


/**
 * Jump-game
 *
 * NeHe Lesson #2 (JOGL 2 Port): Basic 2D Shapes
 *
 * @author Ådne Nystuen, based on code from Hock-Chuan Chua and Thomas Holt
 * @version October 2018
 */

/* Main class which extends GLCanvas. This means that this is a OpenGL canvas.
   We will use OpenGL commands to draw on this canvas.
   This implementation has no animation or user input.
*/
public class Oving15 extends GLCanvas implements GLEventListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static String TITLE = "Jump-game";
    private static final int CANVAS_WIDTH = 1000;  // width of the drawable
    private static final int CANVAS_HEIGHT = 600; // height of the drawable
    private double gluLookAtX = 0;
    private double gluLookAtZ = 10;
    private double xPosition = 0;
    private double zPosition = 0;
    private double yPosition = 0;
    private double gravity = 650;
    private int direction = 0;
    private boolean retning = false;
    private int z = -40;
    private boolean obs = true;
    private int score = 0;
    private Random rng = new Random();
    private int rngObsX = -40;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private int rotation = 0;
    private boolean forward = true;
    private int move = 1;
    private int gameOverObsY = 27;
    private int highScore = 0;
    private TextRenderer textRenderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
    private  String displayText = "";


    // Setup OpenGL Graphics Renderer
    private GLU glu;  // for the GL Utility

    private GLUT glut = new GLUT();

    /**
     * Constructor to setup the GUI for this Component
     */
    public Oving15() {
        this.addGLEventListener(this);
        this.addKeyListener(new RotateKeyListener());
    }

// ------ Implement methods declared in GLEventListener (init,reshape,display,dispose)          

    /**
     * Called immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     */
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glEnable(GL2.GL_DEPTH_TEST);           // enables depth testing
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
    }

    /**
     * Handler for window re-size event. Also called when the drawable is
     * first set to visible
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (height == 0) height = 1;   // prevent divide by zero
        float aspect = (float) width / height;

        //Set the view port (display area) to cover the entire window
        //gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

        // Enable the model-view transform
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
    }

    private void drawGrid(GL2 gl) {
        gl.glBegin(GL.GL_LINES);
        gl.glPopMatrix();


        for (int i = -40; i < 40; i++) {

            gl.glColor3d(1, 0, 0);
            gl.glVertex3d(i, 0, -40);
            gl.glVertex3d(i, 0, 40);
            if (gameOver) {
                gl.glColor3d(1, 0, 0);
            } else {
                gl.glColor3d(0, 1, 0);
            }
            gl.glVertex3d(-40, 0, i);
            gl.glVertex3d(40, 0, i);
        }
        gl.glPushMatrix();
        gl.glEnd();
    }


    private void drawFeet(GL2 gl) {
        gl.glPushMatrix();


        if (move == 0 && forward) {
            rotation += 45;
            move++;
        } else if (move == 0 && !forward) {
            rotation -= 45;
            move++;
        }

        if (rotation > 0) {
            forward = false;
        } else {
            forward = true;
        }

        gl.glColor3d(1, 0, 0);
        gl.glRotated(90, 1, 0, 0);
        gl.glPushMatrix();
        gl.glRotated(rotation, 1, 0, 0);
        glut.glutSolidCylinder(0.2, 1, 10, 10);
        gl.glPopMatrix();

        gl.glTranslated(0.5, 0.0, 0.0);

        gl.glPushMatrix();
        gl.glRotated(-rotation, 1, 0, 0);
        glut.glutSolidCylinder(0.2, 1, 10, 10);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    private void drawMiddle(GL2 gl) {
        gl.glPushMatrix();
        gl.glColor3d(1, 0, 0);
        gl.glRotated(90, 1, 0, 0);
        gl.glScaled(1.0, 0.5, 1.0);
        gl.glTranslated(0.25, 0.0, -0.5);
        glut.glutSolidCube(1);
        gl.glPopMatrix();
    }

    private void drawHead(GL2 gl) {
        gl.glPushMatrix();
        gl.glColor3d(150, 200, 200);
        gl.glTranslated(0.25, 1.25, 0.0);
        glut.glutSolidSphere(0.3, 10, 10);
        gl.glColor3d(1, 0, 0);
        gl.glTranslated(0.15, 0.0, -0.3);
        glut.glutSolidSphere(0.07, 10, 10);
        gl.glTranslated(-0.25, 0.0, 0.0);
        glut.glutSolidSphere(0.07, 10, 10);
        gl.glPopMatrix();
    }

    private void drawArms(GL2 gl) {
        gl.glPushMatrix();
        gl.glColor3d(150, 200, 200);
        gl.glScaled(0.2, 1.0, 0.2);
        gl.glTranslated(4.0, 0.5, 0.0);
        glut.glutSolidCube(1);
        gl.glTranslated(-5.5, 0.0, 0.0);
        glut.glutSolidCube(1);
        gl.glPopMatrix();
    }

    private void gravity(GL2 gl) {

        if (retning && yPosition < 1) {
            yPosition += (1 - yPosition) * (gravity / 7000) + 0.02;
            //  yPosition += gravity/5000;
        } else if (retning && yPosition > 1) {
            retning = false;
        }

        if (!retning && yPosition > 0) {
            yPosition -= (1 - yPosition) * (gravity / 6000) + 0.02;
        } else if (yPosition < 0) {
            yPosition = 0;
        }

    }

    private void drawSides(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslated(12.5, 0, -17.5);
        gl.glScaled(0.5, 0.5, 40);
        glut.glutSolidCube(1);
        gl.glTranslated(-50, 0, 0);
        glut.glutSolidCube(1);

        gl.glPopMatrix();
    }

    private void drawObs(GL2 gl) {
        gl.glPushMatrix();

        if (gameStarted && !gameOver) {
            if (z < 30) {
                z++;
                gl.glTranslated(0, 0, z);
            } else if (z == 30) {
                z = -40;
            }

            gl.glScaled(5.0, 0.15, 0.2);
            glut.glutSolidCube(5);
        }


        gl.glPopMatrix();
    }

    public void score() {
        if (xPosition == z && yPosition >= 0.2) {
            score++;
            System.out.println("Score: " + score);
            displayText = "Score: " + score + ".";
        } else if (xPosition == z && yPosition <= 0.2) {
            displayText = "GameOver.\nDin score ble " + score;
            System.out.println("GameOver.\nDin score ble " + score);
            gameOverObsY = 27;

            if (score > highScore) {
                displayText = "Ny highscore!\nDin forrige highscore: " + highScore + "\nTrykk på 'o' igjen hvis du ønsker å prøve igjen.";
                System.out.println("Ny highscore!\nDin forrige highscore: " + highScore + "\nTrykk på 'o' igjen hvis du ønsker å prøve igjen.");
                highScore = score;
                score = 0;
            }

            gameOver = true;
        }
    }


    public void gameOver(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslated(0, gameOverObsY, -30);
        gl.glScaled(5, 10, 0.2);
        glut.glutSolidCube(5);

        if (gameOver) {
            gameOverObsY -= (29 - gameOverObsY) * (gravity / 6000) + 0.02;
        }
        gl.glPopMatrix();
    }


    /**
     * Called by OpenGL for drawing
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
        gl.glLoadIdentity();  // reset the model-view matrix
        glu.gluLookAt(gluLookAtX, 5, gluLookAtZ, 0, 0, 0, 0, 1, 0);
        drawGrid(gl);
        gameOver(gl);
        drawSides(gl);
        drawObs(gl);
        if (!gameOver) {
            score();
        }

        if(!gameStarted) {
            displayText = "Welcome! Move around using 'WASD' and jump with 'e'. \nPress 'o' to start the game.a";
        }
            textRenderer.beginRendering(1000, 600);
            textRenderer.setColor(Color.YELLOW);
            textRenderer.setSmoothing(true);
            textRenderer.draw(displayText, (int) (0), (int) (0));
            textRenderer.endRendering();

        // drawAllAxis(gl);

        gl.glTranslated(xPosition, yPosition, zPosition);
        gl.glRotated(direction, 0, 1, 0);
        gl.glTranslated(0.0, 1.0, 0.0);
        gravity(gl);
        drawFeet(gl);
        drawMiddle(gl);
        drawHead(gl);
        drawArms(gl);
    }

    /**
     * Called before the OpenGL context is destroyed. Release resource such as buffers.
     */
    public void dispose(GLAutoDrawable drawable) {
    }


    private class RotateKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == '1') {
                if (gluLookAtX > 0) {
                    gluLookAtX = 0;
                    gluLookAtZ = 10;
                } else {
                    gluLookAtX = 10;
                    gluLookAtZ = 0;
                }
            }

            if (e.getKeyChar() == 'd') {
                xPosition++;
                direction = -90;
                move = 0;
            } else if (e.getKeyChar() == 'a') {
                xPosition--;
                direction = 90;
                move = 0;
            } else if (e.getKeyChar() == 's') {
                zPosition++;
                direction = 190;
                move = 0;
            } else if (e.getKeyChar() == 'w') {
                zPosition--;
                direction = 0;
                gluLookAtX = 0;
                gluLookAtZ = 10;
                move = 0;
            } else if (e.getKeyChar() == 'e' && yPosition == 0) {
                retning = true;
            } else if (e.getKeyChar() == 'o' && !gameStarted && !gameOver) {
                gameStarted = true;
            } else if (e.getKeyChar() == 'o' && gameStarted && gameOver) {
                gameStarted = false;
                gameOver = false;
                gameStarted = true;
            }
            Oving15.this.repaint();
        }
    }

    /**
     * The entry main() method to setup the top-level JFrame with our OpenGL canvas inside
     */
    public static void main(String[] args) {
        System.out.println("Trykk på bokstaven \"o\" for å starte spillet!");
        GLCanvas canvas = new Oving15();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));


        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();

        final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
        frame.getContentPane().add(canvas);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//stop program
        frame.setTitle(TITLE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }


    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }


}
