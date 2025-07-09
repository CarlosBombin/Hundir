package Persistencia.Interfaces;

import java.util.List;

import Partida.Partida;

public interface LeerPartidas extends LeerDatos{
    public List<Partida> leer();
}
