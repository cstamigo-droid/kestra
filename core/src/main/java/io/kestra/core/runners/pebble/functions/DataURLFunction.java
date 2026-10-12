package io.kestra.core.runners.pebble.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import io.micronaut.core.naming.NameUtils;
import io.micronaut.http.MediaType;
import io.pebbletemplates.pebble.template.EvaluationContext;
import jakarta.inject.Singleton;

@Singleton
public class DataURLFunction extends AbstractFileFunction {
    public static final String NAME = "dataURL";

    private static final String ERROR_MESSAGE = "The 'dataURL' function expects an argument 'path' that is a path to a namespace file or an internal storage URI.";

    @Override
    public Map<String, String> getArgumentDefaults() {
        HashMap<String, String> defaults = new HashMap<>();
        defaults.put(PATH, "'a/namespace/file'");
        defaults.put(NAMESPACE, "flow.namespace");
        return defaults;
    }

    @Override
    protected Object fileFunction(EvaluationContext context, URI path, String namespace, String tenantId, Map<String, Object> args) throws IOException {
        Optional<MediaType> mimeType = MediaType.forExtension(NameUtils.extension(path.toString()).toLowerCase(Locale.ENGLISH));
        if (mimeType.isEmpty()) {
            throw new IllegalArgumentException("Cannot process the URI %s: unrecognized file extension.".formatted(path));
        }

        byte[] bytes;
        try (InputStream inputStream = openStream(NAME, path, namespace, tenantId, args)) {
            bytes = inputStream.readAllBytes();
        }

        return "data:" + mimeType.get() + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    protected String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
