package ar.edu.itba.simulacion.tp0;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CellIndexMethod {
    
    private final int       M;
    private final double    L;
    private final double    actionRadius;
    private final boolean   periodicOutline;

    public CellIndexMethod(final int M, final double L, final double actionRadius, final boolean periodicOutline) {
        this.M = M;
        this.L = L;
        this.actionRadius = actionRadius;
        this.periodicOutline = periodicOutline;
    }

    private int particleCell(final Particle particle) {
        return coordinateToCell(particle.getX(), particle.getY());
    }

    private int coordinateToCell(final double x, final double y) {
        int cellX = (int) (x / (L / M));
        int cellY = (int) (y / (L / M));
        return cellY * M + cellX;
    }

    public Map<Integer, Set<Particle>> calculateNeighbours(final List<Particle> particles) {
        final Map<Integer, Set<Particle>> ret = new HashMap<>(particles.size());

        // Inicializamos mapa de respuesta
        for(final Particle particle : particles) {
            ret.put(particle.getId(), new HashSet<>());
        }

        // Inicializamos celdas
        final Map<Integer, List<Particle>> cells = buildCells(particles);

        cells.forEach((cell, cellValues) -> {
            for(final Particle particle : cellValues) {
                // Agregamos las particulas de la misma celda
                addNeighbours(particle, cellValues, ret);

                // Agregamos las particulas de las celdas vecinas
                listCellNeighbours(cell, neighbourCell -> addNeighbours(particle, cells.get(neighbourCell), ret));
            }
        });

        return ret;
    }

    private Map<Integer, List<Particle>> buildCells(final List<Particle> particles) {
        final Map<Integer, List<Particle>> ret = new HashMap<>(M * M);

        // Inicializamos todas las celdas en el mapa, asignandoles un id unico segun su posicion
        for(int i = 0; i < M * M; i++){
            ret.put(i, new ArrayList<>());
        }

        // Distribuimos las particulas en la celda correspondiente
        for(final Particle particle : particles) {
            ret.get(particleCell(particle)).add(particle);
        }

        return ret;
    }

    private void addNeighbours(final Particle particle, final List<Particle> potentialNeighbours, final Map<Integer, ? extends Collection<Particle>> cellsNeighbours) {
        final Collection<Particle> currentNeighbours = cellsNeighbours.get(particle.getId());

        for(final Particle neighbour: potentialNeighbours) {
            if(!particle.equals(neighbour) && !currentNeighbours.contains(neighbour) && particle.distanceTo(neighbour) < actionRadius) {
                currentNeighbours.add(neighbour);
                cellsNeighbours.get(neighbour.getId()).add(particle);
            }
        }
    }

    // Como optimizacion, solo listamos la mitad de los vecinos
    // Como todos listan la misma mitad, todos terminan siendo listados
    private void listCellNeighbours(final int cell, final Consumer<Integer> consumer) {
        int cellPositionX = cell % M;
        int cellPositionY = (int) (cell / L);

        // Top
        if(periodicOutline || cellPositionY + 1 < M) {
            consumer.accept(coordinateToCell(cellPositionX, cellPositionY + 1 % M));
        }
        // Top-Right
        if(periodicOutline || (cellPositionY + 1 < M && cellPositionX + 1 < M)) {
            consumer.accept(coordinateToCell(cellPositionX + 1 % M, cellPositionY + 1 % M));
        }
        // Right
        if(periodicOutline || cellPositionX + 1 < M) {
            consumer.accept(coordinateToCell(cellPositionX + 1 % M, cellPositionY));
        }
        // Bottom-Right
        if (periodicOutline || (cellPositionY - 1 < 0 && cellPositionX + 1 < M)) {
            consumer.accept(coordinateToCell(cellPositionX + 1 % M, cellPositionY - 1 % M));
        }
    }
}