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

public class NettyResourceTransformer implements ReproducibleResourceTransformer {

    // A map of resource file paths to be modified
    private final Map<String, String> resources = new TreeMap<>();

    private long time = Long.MIN_VALUE;

    public NettyResourceTransformer() {
    }

    @Override
    public boolean canTransformResource(String resource) {
        return resource.startsWith("META-INF/native-image/io.netty");
    }

    @Override
    public void processResource(final String resource, final InputStream is, final List<Relocator> relocators, final long time) throws IOException {

        String newResource = resource.replace("io.netty", "io.grpc.netty.shaded.io.netty");
        String newContent = IOUtils.toString(is, StandardCharsets.UTF_8).replace("io.netty", "io.grpc.netty.shaded.io.netty");
        resources.put(newResource, newContent);
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
        for (Map.Entry<String, String> resource : resources.entrySet()) {
            String key = resource.getKey();
            String data = resource.getValue();

            JarEntry jarEntry = new JarEntry(key);
            jarEntry.setTime(time);
            jos.putNextEntry(jarEntry);

            IOUtils.write(data, jos, StandardCharsets.UTF_8);
            jos.flush();
        }
    }
}
