package games.findmurderer.gui;

import core.components.GridBoard;
import games.findmurderer.MurderGameState;
import games.findmurderer.components.Person;
import gui.ScreenHighlight;
import gui.views.ComponentView;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import static gui.GUI.defaultItemSize;

public class MurderBoardView extends ComponentView implements ScreenHighlight {

    boolean highlightKiller = false;

    HashMap<Person.Status, Color> statusColorMap = new HashMap<Person.Status, Color>() {{
        put(Person.Status.Alive, new Color(113, 225, 75));
        put(Person.Status.Dead, new Color(229, 134, 112));
    }};
    Color killerOutline = new Color(0, 113, 183);
    Color detectiveKillHighlight = new Color(196, 107, 86);
    Stroke killerStroke;

    Rectangle[] rects;  // Used for highlights + action trimming
    ArrayList<Rectangle> highlight;
    HashMap<Rectangle, Integer> rectToComponentIDMap;
    int itemSize = defaultItemSize;
    int maxHeight = 500;

    public MurderBoardView(GridBoard<Person> gridBoard) {
        super(gridBoard, gridBoard.getWidth() * defaultItemSize, gridBoard.getHeight() * defaultItemSize);
        rects = new Rectangle[gridBoard.getWidth() * gridBoard.getHeight()];
        highlight = new ArrayList<>();
        rectToComponentIDMap = new HashMap<>();

        if (gridBoard.getHeight()*itemSize > maxHeight) {
            itemSize = Math.max(1,maxHeight/gridBoard.getHeight());
            this.height = itemSize * gridBoard.getHeight();
            this.width = itemSize * gridBoard.getWidth();
        }
        killerStroke = new BasicStroke((int)(6 * itemSize*1.0/defaultItemSize));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Left click, highlight cell
                    for (Rectangle r: rects) {
                        if (r != null && r.contains(e.getPoint())) {
                            highlight.clear();
                            highlight.add(r);
                            break;
                        }
                    }
                } else {
                    // Remove highlight
                    highlight.clear();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void paintComponent(Graphics g) {
        drawGridBoard((Graphics2D)g, (GridBoard<Person>) component, 0, 0);

        if (highlight.size() > 0) {
            g.setColor(Color.green);
            Stroke s = ((Graphics2D) g).getStroke();
            ((Graphics2D) g).setStroke(new BasicStroke(3));

            Rectangle r = highlight.get(0);
            g.drawRect(r.x, r.y, r.width, r.height);
            ((Graphics2D) g).setStroke(s);
        }
    }

    public void drawGridBoard(Graphics2D g, GridBoard<Person> gridBoard, int x, int y) {
        int width = gridBoard.getWidth() * itemSize;
        int height = gridBoard.getHeight() * itemSize;

        // Draw background
        g.setColor(Color.white);
        g.fillRect(x, y, width-1, height-1);

        // Draw cells
        for (int i = 0; i < gridBoard.getHeight(); i++) {
            for (int j = 0; j < gridBoard.getWidth(); j++) {
                int xC = x + j * itemSize;
                int yC = y + i * itemSize;
                Person p = gridBoard.getElement(j, i);
                drawCell(g, p, xC, yC);

                // Save rect where cell is drawn
                int idx = i * gridBoard.getWidth() + j;
                if (rects[idx] == null) {
                    rects[idx] = new Rectangle(xC, yC, itemSize, itemSize);
                    if (p != null) {
                        rectToComponentIDMap.put(rects[idx], p.getComponentID());
                    }
                }
            }
        }
    }

    private void drawCell(Graphics2D g, Person element, int x, int y) {
        // Paint cell background
        g.setColor(Color.white);
        g.fillRect(x, y, itemSize, itemSize);
        g.setColor(Color.black);
        g.drawRect(x, y, itemSize, itemSize);

        int pad = (int)(5 * itemSize*1.0/defaultItemSize);

        // Paint element in cell
        if (element != null) {
            Font f = g.getFont();
            Stroke s = g.getStroke();

            g.setColor(statusColorMap.get(element.status));
            g.fillOval(x+pad, y+pad, itemSize-pad*2, itemSize-pad*2);

            if (element.killer == MurderGameState.PlayerMapping.Detective) {
                g.setColor(detectiveKillHighlight);
                g.setFont(new Font(f.getName(), Font.BOLD, itemSize));
                int w = g.getFontMetrics().stringWidth("X");
                g.drawString("X", x + itemSize / 2 - w / 2, y + itemSize / 2 + itemSize/3);
            }

            g.setColor(Color.black);
            g.setFont(new Font(f.getName(), Font.BOLD, (int)(12 * itemSize*1.0/defaultItemSize)));
            String name = ""+element.getComponentID();  // element.toString()
            int w = g.getFontMetrics().stringWidth(name);
            g.drawString(name, x + itemSize/2 - w/2, y + itemSize/2 + (int)(6 * itemSize*1.0/defaultItemSize));
            g.setFont(f);

            if (highlightKiller && element.personType == Person.PersonType.Killer) {
                g.setColor(killerOutline);
                g.setStroke(killerStroke);
                g.drawOval(x + pad, y + pad, itemSize - pad * 2, itemSize - pad * 2);
                g.setStroke(s);
            }
        }
    }

    public ArrayList<Rectangle> getHighlight() {
        return highlight;
    }

    public HashMap<Rectangle, Integer> getRectToComponentIDMap() {
        return rectToComponentIDMap;
    }

    @Override
    public void clearHighlights() {
        highlight.clear();
    }
}