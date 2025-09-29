package src.pas.pacman.agents;


import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
// SYSTEM IMPORTS
import java.util.Random;
import java.util.Set;
import java.util.Stack;

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
        return null;
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
        return 1f;
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

  
        Set<Coordinate> coords = new HashSet<>();
        final Action[] directions = {Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST};
        int[] x = {0, 1, 0, -1};
        int[] y = {1, 0, -1, 0};

        for(int i = 0; i < 4; i++){
            int nx = src.getXCoordinate() + x[i];
            int ny = src.getYCoordinate() + y[i];
            System.out.println(i);
            System.out.println(src.toString());
            Coordinate next = new Coordinate(nx, ny);
            System.out.println(next.toString());
            System.out.println("\n");
            if(game.isLegalPacmanMove(src, directions[i])){
                System.out.println(1);
                if(nx >= 0 && ny >=0){

                coords.add(next);
            }
            }
        }

        return coords;
    }

    @Override
    public Path<Coordinate> graphSearch(final Coordinate src,
                                        final Coordinate tgt,
                                        final GameView game)
    {
        Queue<Path<Coordinate>> paths = new LinkedList<>();
        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> coords = new HashSet<>();
        Path<Coordinate> start = new Path<>(src, 0f, null);

        paths.add(start);
        visited.add(src);

        while(!paths.isEmpty())
        {
            Path<Coordinate> current = paths.remove();
            Coordinate at = current.getDestination();

            if(current.getDestination().equals(tgt)) 
            {
                return current;
            }

            coords = getOutgoingNeighbors(at, game);

            for(Coordinate coord: coords)
            {
                if (visited.add(coord)) 
                {
                    paths.add(new Path<>(coord, 1f, current));
                }
            }
        }

        return new Path<>(src, 0f, null);
    }

    @Override
    public void makePlan(final GameView game)
    {
        Coordinate src = game.getEntity(getMyEntityId()).getCurrentCoordinate(); // getting the source
        Path<Coordinate> path = graphSearch(src, getTargetCoordinate(), game);
        Stack<Coordinate> stack = new Stack<>();

        while(path!=null) // turning into stack
        {
            stack.push(path.getDestination());
            path = path.getParentPath();
        }

        setPlanToGetToTarget(stack);
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
