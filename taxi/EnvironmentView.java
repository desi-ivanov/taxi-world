package taxi;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

enum ElType {
  TAXI,
  PEDESTRIAN,
  DESIRED_POS,
}

public class EnvironmentView extends JFrame {
  static int GRID_SZ = 20;

  private static final long serialVersionUID = 1L;

  protected int cellSize = 0;

  protected GridCanvas drawArea;
  private List<Pair<Pos, Pair<String, ElType>>> elements = new LinkedList<>();

  protected Font defaultFont = new Font("Arial", Font.BOLD, 10);

  public EnvironmentView(String title, int windowSize) {
    super(title);
    initComponents(windowSize);
    setVisible(true);
    repaint();
  }

  public void initComponents(int width) {
    setSize(width, width);
    getContentPane().setLayout(new BorderLayout());
    drawArea = new GridCanvas();
    getContentPane().add(BorderLayout.CENTER, drawArea);
  }

  @Override
  public void repaint() {
    cellSize = drawArea.getWidth() / GRID_SZ;
    super.repaint();
    drawArea.repaint();
  }

  public void flush(List<Pair<Pos, Pair<String, ElType>>> elements) {
    this.elements = elements;
    repaint();
  }

  public void drawBlock(Graphics g, int x, int y, ElType c, String name) {
    g.setColor(c == ElType.TAXI ? Color.YELLOW : c == ElType.PEDESTRIAN ? Color.ORANGE : Color.GREEN);
    if (c == ElType.DESIRED_POS) {
      int radius = cellSize / 2;
      g.fillOval(x * cellSize + radius / 2, y * cellSize + radius / 2, radius, radius);
    } else {
      g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
    }

    g.setColor(Color.black);
    drawString(g, x, y, defaultFont, c == ElType.DESIRED_POS ? name.charAt(name.length() - 1) + "" : name);
  }

  public void drawString(Graphics g, int x, int y, Font f, String s) {
    g.setFont(f);
    FontMetrics metrics = g.getFontMetrics();
    int width = metrics.stringWidth(s);
    int height = metrics.getHeight();
    g.drawString(s, x * cellSize + (cellSize / 2 - width / 2), y * cellSize + (cellSize / 2 + height / 2));
  }

  public Canvas getCanvas() {
    return drawArea;
  }

  class GridCanvas extends Canvas {

    private static final long serialVersionUID = 1L;

    public void paint(Graphics g) {
      cellSize = drawArea.getWidth() / GRID_SZ;
      cellSize = drawArea.getHeight() / GRID_SZ;

      g.setColor(Color.lightGray);
      for (int l = 1; l <= GRID_SZ; l++) {
        g.drawLine(0, l * cellSize, GRID_SZ * cellSize, l * cellSize);
      }
      for (int c = 1; c <= GRID_SZ; c++) {
        g.drawLine(c * cellSize, 0, c * cellSize, GRID_SZ * cellSize);
      }

      elements.forEach(el -> {
        drawBlock(g, el.fst.x, el.fst.y, el.snd.snd, el.snd.fst);
      });
    }
  }
}
