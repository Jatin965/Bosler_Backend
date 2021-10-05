package io.bosler.kitab.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kitab")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class kitabController {

    private final FolderRepository folderRepository;
    private final DatasetRepository datasetRepository;


    @Operation(summary = "It provides list of all kitab.")
    @GetMapping("/all")
    public ResponseEntity<List<FolderModel>> getAll() {

        List<FolderModel> all = folderRepository.findAll();
        return ResponseEntity.ok().body(all);
    }


    @Operation(summary = "It provides list of all projects")
    @GetMapping("/project/all")
    public ResponseEntity<List<FolderModel>> getProjects() {

        List<FolderModel> allProjects = folderRepository.getByType("project");
        return ResponseEntity.ok().body(allProjects);
    }

    @Operation(summary = "This endpoint can be used to create / update projects")
    @PostMapping("/project/create")
    public ResponseEntity<?> newProject(@Valid @RequestBody FolderModel newProject) {

        List<FolderModel> children = folderRepository.findAllByName(newProject.name);

        if(children.stream().anyMatch(project -> newProject.name.equals(project.name))) {
            return new ResponseEntity<>("Error: Same name project exists!", HttpStatus.NOT_ACCEPTABLE);
        }

        newProject.parent = new UUID(0, 0);
        newProject.type = "project";
        newProject.status = "active";

        FolderModel project = folderRepository.save(newProject);

        FolderModel folder = new FolderModel();

        FolderModel dataFolder = folderRepository.save(new FolderModel(null,
                "Data",
                "Data folder",
                "folder",
                project.id,
                "active",
                new Date(),
                new Date(),
                "Bosler",
                "Bosler"
        ));

        FolderModel documentationFolder = folderRepository.save(new FolderModel(null,
                "Documentation",
                "Documentation folder",
                "folder",
                project.id,
                "active",
                new Date(),
                new Date(),
                "Bosler",
                "Bosler"
        ));

        FolderModel logicFolder = folderRepository.save(new FolderModel(null,
                "Logic",
                "Logic folder",
                "folder",
                project.id,
                "active",
                new Date(),
                new Date(),
                "Bosler",
                "Bosler"
        ));

        folderRepository.save(new FolderModel(null,
                "View",
                "View folder",
                "folder",
                project.id,
                "active",
                new Date(),
                new Date(),
                "Bosler",
                "Bosler"
        ));


        return new ResponseEntity<> (project, HttpStatus.OK);
    }

    @Operation(summary = "It delete project by ID")
    @DeleteMapping(path = "/Project/{Id}/delete")
    public ResponseEntity<Object> deleteProject(@PathVariable("Id") UUID Id) {
        boolean exists = folderRepository.existsById(Id);

        Map<String, String> resultTemplate = new HashMap<>();

        if (!exists) {
            resultTemplate.put("error", "Id " + Id + " does not exits!");
            return new ResponseEntity<Object>(resultTemplate, HttpStatus.NOT_FOUND);
        }

        folderRepository.deleteById(Id);

        resultTemplate.put("success", "Id " + Id + " deleted successfully!");

        return new ResponseEntity<>(resultTemplate, HttpStatus.OK);
    }

    @Operation(summary = "It provides project by Name")
    @GetMapping("/project/{name}")
    public ResponseEntity<FolderModel>getProjectByName(@PathVariable("name") String name) {

        //List  = folderRepository.getByName(byname);
        //if(!exists) {
        //    return new ResponseEntity<>((FolderModel) folderRepository.getByName(byname), HttpStatus.NOT_FOUND);
        //}
        return new ResponseEntity<>(folderRepository.getByName(name), HttpStatus.OK);
    }

    @Operation(summary = "It provides project by Id")
    @GetMapping("/project/{Id}")
    public ResponseEntity<FolderModel> getProjectById(@PathVariable("Id") UUID Id) {

        boolean exists = folderRepository.existsById(Id);
        if (!exists) {
            return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @Operation(summary = "It rename project by name")
    @GetMapping(path = "/project/{name}/{newName}/rename")
    public ResponseEntity<?> renameProject(
            @PathVariable("name") String name,
            @PathVariable("newName") String newName) {

        FolderModel project = folderRepository.getByName(name);
        project.name = newName;
        folderRepository.save(project);
        // if (!folderRepository.getByname(Name)){
        return new ResponseEntity<>("Project rename successfully", HttpStatus.OK);
        // }
    }

    @Operation(summary = "Provides logical path based on Id")
    @GetMapping(path = "/getPath/{Id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getPath(
            @PathVariable("Id") UUID Id) {

        if (!folderRepository.existsById(Id)) {
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "Id " + Id + " does not exits!");
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        List<FolderModel> path = getPathById(Id, new ArrayList<>());




        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
        Gson gson = b.create();
        String json = gson.toJson(path); // converts to json

        return new ResponseEntity<>(json, HttpStatus.OK);
    }


    List<FolderModel> getPathById(UUID id, List<FolderModel> path) {

        FolderModel pathId = folderRepository.getById(id);

        path.add(pathId);

        if (!Objects.equals(pathId.getParent(), new UUID(0, 0))) {
            getPathById(pathId.getParent(), path);
        }
        return path;
    }



    /**
     * This TypeAdapter unproxies Hibernate proxied objects, and serializes them
     * through the registered (or default) TypeAdapter of the base class.
     */
    public static class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {

        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return (HibernateProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
            }
        };
        private final Gson context;

        private HibernateProxyTypeAdapter(Gson context) {
            this.context = context;
        }

        @Override
        public HibernateProxy read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void write(JsonWriter out, HibernateProxy value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            // Retrieve the original (not proxy) class
            Class<?> baseType = Hibernate.getClass(value);
            // Get the TypeAdapter of the original class, to delegate the serialization
            TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
            // Get a filled instance of the original class
            Object unproxiedValue = ((HibernateProxy) value).getHibernateLazyInitializer()
                    .getImplementation();
            // Serialize the value
            delegate.write(out, unproxiedValue);
        }
    }

}

