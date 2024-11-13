package com.github.fridrich.maven.shade.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ReproducibleResourceTransformer;

public class NettyResourceRenamer implements ReproducibleResourceTransformer {

    // A map of resource file paths to be modified
    private final Map<String, byte[]> resources = new TreeMap<>();

    private long time = Long.MIN_VALUE;

    public NettyResourceRenamer() {
    }

    @Override
    public boolean canTransformResource(String resource) {
        return resource.startsWith("META-INF/native/libnetty") || resource.startsWith("META-INF/native/netty");
    }

    @Override
    public void processResource(final String resource, final InputStream is, final List<Relocator> relocators, final long time) throws IOException {

        String newResource = resource
            .replace("netty", "io_grpc_netty_shaded_netty");
        byte[] content = IOUtils.toByteArray(is);
        resources.put(newResource, content);
    }

    @Override
    public void processResource(final String resource, final InputStream is, final List<Relocator> relocators) throws IOException {
        processResource(resource, is, relocators, 0L);
    }

    @Override
    public boolean hasTransformedResource() {
        return !resources.isEmpty();
    }

    @Override
    public void modifyOutputStream(JarOutputStream jos) throws IOException {
        for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
            String key = resource.getKey();
            byte[] data = resource.getValue();

            JarEntry jarEntry = new JarEntry(key);
            jarEntry.setTime(time);
            jos.putNextEntry(jarEntry);

            IOUtils.write(data, jos);
            jos.flush();
        }
    }
}
