package src.pas.pacman.agents;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
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
        Set<PelletVertex> neighbors = new HashSet<>();
        final Set<Coordinate> pellets = vertex.getRemainingPelletCoordinates();

        if(pellets==null || pellets.isEmpty()) 
        {
            return neighbors;
        }

        for(Coordinate pellet: pellets) 
        {
            PelletVertex temp = vertex.removePellet(pellet);
            neighbors.add(temp);
        }

        return neighbors;
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
                               final PelletVertex dst)
    {
        final Set<Coordinate> s = src.getRemainingPelletCoordinates();
        final Set<Coordinate> d = dst.getRemainingPelletCoordinates();
        final Coordinate pac = src.getPacmanCoordinate();
        Coordinate removed = null;
        int diff = 0;
        float w;

        for(Coordinate c: s) 
        {
            if(!d.contains(c)) 
            {
                removed = c;
                diff++;
                if(diff == 1) // diff needs to be 1
                {
                    break; 
                }
            }
        }

        if(diff!=1 || removed==null) 
        {
            return Float.POSITIVE_INFINITY;
        }

        w = Math.abs(pac.getXCoordinate()-removed.getXCoordinate()) + Math.abs(pac.getYCoordinate()-removed.getYCoordinate());

        return w;
    }

    @Override
    public float getHeuristic(final PelletVertex src,
                              final GameView game)
    {
        Set<Coordinate> pellets = src.getRemainingPelletCoordinates(); 
        if(pellets==null || pellets.isEmpty()) 
            {
            return 0f;
            }
        Set<Coordinate> nodes = new HashSet<>(pellets);
        Coordinate start = src.getPacmanCoordinate();
        nodes.add(start);
        Set<Coordinate> tree = new HashSet<>();
        Map<Coordinate, Double> minEdge = new HashMap<>();
        for (Coordinate c : nodes) {
        minEdge.put(c, Double.POSITIVE_INFINITY);
        }
        minEdge.put(start, 0.0);
        float total = 0;
        while (tree.size() < nodes.size()) {
        Coordinate best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (Coordinate c : nodes) {
            if (!tree.contains(c) && minEdge.get(c) < bestDist) {
                bestDist = minEdge.get(c);
                best = c;
            }
        }
            if (best == null) break;
    tree.add(best);
    total += bestDist;
    for (Coordinate neighbor : nodes) {
        if (!tree.contains(neighbor)) {
            double dist = Math.abs(best.getXCoordinate() - neighbor.getXCoordinate()) + 
                            Math.abs(best.getYCoordinate() - neighbor.getYCoordinate());
            
            if (dist < minEdge.get(neighbor)) {
                minEdge.put(neighbor, dist);
            }
        }
    }
}





       
        
        return total;
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
        int[] y = {-1, 0, 1, 0};
        int i = 0;

        for(Action dir: directions){
            int nx = src.getXCoordinate() + x[i];
            int ny = src.getYCoordinate() + y[i];
            Coordinate next = new Coordinate(nx, ny);
            if(game.isLegalPacmanMove(src, dir)&& game.isInBounds(next)){
                coords.add(next);
            }
            i++;
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
