package Persistencia.Interfaces;

import java.util.List;
import Cliente.Usuario;

public interface LeerUsuarios extends LeerDatos{
    public List<Usuario> leer();
}
