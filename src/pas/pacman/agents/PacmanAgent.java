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
        if (s.size()!=d.size()+1)
        {
            return Float.POSITIVE_INFINITY;
        }
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
                if(diff>1) // diff needs to be 1
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
        PelletVertex start = new PelletVertex(game);
        Map<PelletVertex, Float> gScore = new HashMap<>();
        Path<PelletVertex> startNode = new Path<>(start, 0f, null);
        PriorityQueue<Path<PelletVertex>> openSet;
        Map<PelletVertex, Float> hCache = new HashMap<>(); 
        Set<PelletVertex> closed = new HashSet<>(); 
        Map<String, Float> distCache = new HashMap<>();

        Comparator<Path<PelletVertex>> pathComparator = new Comparator<Path<PelletVertex>>()
        {
            @Override
            public int compare(Path<PelletVertex> a, Path<PelletVertex> b)
            {
                float fA = a.getTrueCost() + a.getEstimatedPathCostToGoal(); 
                float fB = b.getTrueCost() + b.getEstimatedPathCostToGoal();  
                return Float.compare(fA, fB);
            }
        };

        openSet = new PriorityQueue<>(pathComparator);

        if(start.getRemainingPelletCoordinates().isEmpty())
        {
            return new Path<>(start, 0f, null);
        }

        float hStart = getHeuristic(start, game);
        hCache.put(start, hStart);
        startNode.setEstimatedPathCostToGoal(hStart);
        openSet.add(startNode);
        gScore.put(start, 0f);

        while(!openSet.isEmpty())
        {
            Path<PelletVertex> current = openSet.poll();
            PelletVertex u = current.getDestination();

            if(closed.contains(u))
            {
                continue;
            }
            closed.add(u);

            if(u.getRemainingPelletCoordinates().isEmpty())
            {
                return current;
            }

            Set<PelletVertex> neighbors = getOutoingNeighbors(u, game);

            for(PelletVertex v: neighbors)
            {
                if (closed.contains(v))
                {
                    continue;
                }
                final Set<Coordinate> su = u.getRemainingPelletCoordinates();
                final Set<Coordinate> sv = v.getRemainingPelletCoordinates();
                if(su.size()!=sv.size()+1) 
                { 
                    continue; 
                } 
                Coordinate removed = null;             
                int diff = 0;                         
                for(Coordinate c: su)              
                {                                       
                    if(!sv.contains(c))                
                    {                                   
                        removed = c;                    
                        diff++;                         
                        if (diff > 1) break;            
                    }                                   
                }                                       
                if(diff != 1 || removed == null)       
                {                                       
                    continue;                           
                }                                       

                Coordinate from = u.getPacmanCoordinate();  
                String cacheKey = from.getXCoordinate() + "," + from.getYCoordinate() + ">" + removed.getXCoordinate() + "," + removed.getYCoordinate();  
                Float wObj = distCache.get(cacheKey);   
                float w;                                
                if(wObj == null)                       
                {                                       
                    if(from.equals(removed)) 
                    {
                        w = 0f;
                    }else 
                    {
                        Path<Coordinate> sp = graphSearch(from, removed, game);
                        w = sp.getTrueCost();
                    }
                    distCache.put(cacheKey, w);       
                }                                       
                else                                    
                {                                       
                    w = wObj;                           
                }  
                if(Float.isInfinite(w) || w<0f)
                {
                    continue;
                }

                float newG = current.getTrueCost() + w;
                Float oldG = gScore.get(v);

                if(oldG==null || newG<oldG)
                {
                    float h = hCache.computeIfAbsent(v, pv -> getHeuristic(pv, game));
                    Path<PelletVertex> next = new Path<>(v, w, current);
                    next.setEstimatedPathCostToGoal(h);
                    gScore.put(v, newG); 
                    openSet.add(next);
                }
            }
        }

        return new Path<>(start, 0f, null);
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

        while(path!=null && path.getParentPath()!=null) // turning into stack
        {
            stack.push(path.getDestination());
            path = path.getParentPath();
        }

        setPlanToGetToTarget(stack);
    }

    @Override
    public Action makeMove(final GameView game)
    {
        Stack<Coordinate> plan = getPlanToGetToTarget();
        if (plan==null || plan.isEmpty()) 
        { 
            return Action.values()[this.getRandom().nextInt(Action.values().length)];
        }
        Coordinate curr = game.getEntity(getMyEntityId()).getCurrentCoordinate();
        Coordinate next = plan.peek();
        int dx;
        int dy;

        if(curr.equals(next))
        {
            plan.pop();
            if (plan.isEmpty()) 
            { 
                return Action.values()[this.getRandom().nextInt(Action.values().length)];
            }
            next = plan.peek();
        }

        dx = next.getXCoordinate() - curr.getXCoordinate();
        dy = next.getYCoordinate() - curr.getYCoordinate();

        if(dx==1 && dy==0)  
        { 
            return Action.EAST; 
        }
        if(dx==-1 && dy==0) 
        { 
            return Action.WEST; 
        }
        if(dx==0 && dy==-1) 
        { 
            return Action.NORTH; 
        }
        if(dx==0 && dy==1)  
        { 
            return Action.SOUTH;
        }
        makePlan(game);

        return Action.values()[this.getRandom().nextInt(Action.values().length)];
    }

    @Override
    public void afterGameEnds(final GameView game)
    {

    }
}
