
package TechShop.Virginia.repository;

import TechShop.Virginia.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    public List<Producto> findByActivoTrue();
    
}
