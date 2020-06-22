package example.springdata.gremlin.domain;

import com.microsoft.spring.data.gremlin.annotation.EdgeSet;
import com.microsoft.spring.data.gremlin.annotation.Graph;
import com.microsoft.spring.data.gremlin.annotation.VertexSet;
import lombok.Getter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Graph
public class Network {

    @Id
    private String id;

    public Network() {
        this.edges = new ArrayList<Object>();
        this.vertexes = new ArrayList<Object>();
    }

    @EdgeSet
    @Getter
    private List<Object> edges;

    @VertexSet
    @Getter
    private List<Object> vertexes;
}
