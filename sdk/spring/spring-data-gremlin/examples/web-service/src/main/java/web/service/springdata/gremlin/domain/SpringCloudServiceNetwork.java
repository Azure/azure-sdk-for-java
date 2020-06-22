package web.service.springdata.gremlin.domain;

import com.microsoft.spring.data.gremlin.annotation.EdgeSet;
import com.microsoft.spring.data.gremlin.annotation.Graph;
import com.microsoft.spring.data.gremlin.annotation.VertexSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Graph
@NoArgsConstructor
public class SpringCloudServiceNetwork {

    @Id
    private String id;

    @EdgeSet
    @Getter
    private List<Object> edges = new ArrayList<>();

    @VertexSet
    @Getter
    private List<Object> vertexes = new ArrayList<>();
}
