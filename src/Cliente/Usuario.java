package Cliente;

public class Usuario {
    private String name;
    private String password;
    private boolean esNuevo;
    
    public Usuario(String name, String password) {
        this.name = name;
        this.password = password;
        this.esNuevo = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean esNuevo() {
        return esNuevo;
    }
    
    public void setNuevo(boolean esNuevo) {
        this.esNuevo = esNuevo;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Usuario usuario = (Usuario) obj;
        return name != null ? name.equals(usuario.name) : usuario.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
