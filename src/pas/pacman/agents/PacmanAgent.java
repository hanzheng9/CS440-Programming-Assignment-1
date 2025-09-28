package src.pas.pacman.agents;


import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
// SYSTEM IMPORTS
import java.util.Random;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.agents.SearchAgent;
import edu.bu.pas.pacman.interfaces.ThriftyPelletEater;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;
public class PacmanAgent
    extends SearchAgent
    implements ThriftyPelletEater
{

    private final Random random;
    private Coordinate goal;

    public PacmanAgent(int myUnitId,
                       int pacmanId,
                       int ghostChaseRadius)
    {
        super(myUnitId, pacmanId, ghostChaseRadius);
        this.random = new Random();
    }

    public final Random getRandom() { return this.random; }

    @Override
    public Set<PelletVertex> getOutoingNeighbors(final PelletVertex vertex,
                                                 final GameView game)
    {
        Set<PelletVertex> coords = new HashSet<>();
        String[] directions =  {"NORTH", "SOUTH", "EAST", "WEST"};
         for(int i = 0; i < 4; i++){
            if(game.isLegalMove(this.getPacmanId(), this.makeMove(this.getPacmanId(), Action.valueOf(directions[i])))){
            }
        }
        return coords;
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
                               final PelletVertex dst)
    {
        return 1f;
    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game)
    {
        int dx = Math.abs(src.getX() - goal.getX());
        int dy = Math.abs(src.getY() - goal.getY());
        return dx + dy;
    }

    @Override
    public Path<PelletVertex> findPathToEatAllPelletsTheFastest(final GameView game)
    {
        return null;
    }

    @Override
    public Set<Coordinate> getOutgoingNeighbors(final Coordinate src,
                                                final GameView game)
    {
        return null;
    }

    @Override
    public Path<Coordinate> graphSearch(final Coordinate src,
                                        final Coordinate tgt,
                                        final GameView game)
    {
        this.goal = tgt;
        class Node 
        {
            final Coordinate c;
            final float cost;    
            final Node parent;

            Node(Coordinate c, float cost, Node parent) {
                this.c = c; 
                this.cost = cost; 
                this.parent = parent;
            }
        }
        Comparator<Node> nodeComparator = new Comparator<Node>() 
        {
            @Override
            public int compare(Node a, Node b) {
                double totalA = a.cost + getHeuristic(a.c, tgt);
                double totalB = b.cost + getHeuristic(b.c, tgt);
                return Double.compare(totalA, totalB);
            }
        };
        PriorityQueue<Node> open = new PriorityQueue<>(nodeComparator);
        return null;
    }

    @Override
    public void makePlan(final GameView game)
    {

    }

    @Override
    public Action makeMove(final GameView game)
    {
        return Action.values()[this.getRandom().nextInt(Action.values().length)];
    }

    @Override
    public void afterGameEnds(final GameView game)
    {

    }
}
