package io.bosler.fractal.controllers;


import io.bosler.fractal.library.exception.NoSuchElementFoundException;
import io.bosler.fractal.library.models.*;
import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.bosler.fractal.library.services.FractalRepository;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/fractal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fractal", description = "Code Repository management service endpoints")
public class FractalController {

    private final FractalRepository fractalRepository;
    private final GitService gitServiceFractal;
    private final FolderRepository folderRepository;

    @GetMapping("/{RepositoryId}/{NewBranchName}/createBranch")
    public ResponseEntity<String> createBranch(
            @PathVariable("RepositoryId") String RepositoryId,
            @PathVariable("NewBranchName") String NewBranchName) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            Git git = new Git(repository);
            git.branchCreate()
                    .setName(NewBranchName)
                    .call()
                    .getName();
            git.checkout().setCreateBranch(true);
            return new ResponseEntity<>("Branch Created Successfully", HttpStatus.OK);
        } catch (java.lang.IllegalStateException elite) {
            return new ResponseEntity<>("error: create Unsuccessful", HttpStatus.CONFLICT);
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("error: create Unsuccessful", HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{RepositoryId}/{BranchName}/DeleteBranch")
    public ResponseEntity<String> DeleteBranch(
            @PathVariable("RepositoryId") String RepositoryId,
            @PathVariable("BranchName") String branchName) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            Git git = new Git(repository);
            if (repositoryExists(repository) && branchExists(RepositoryId, branchName)) {
                git.branchDelete().setForce(true).setBranchNames(branchName).call();

                return new ResponseEntity<>("Branch deleted Successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("Repository or branch does not exist for id: " + RepositoryId + "or branch: " + branchName, HttpStatus.NOT_FOUND);

        } catch (java.lang.IllegalStateException elite) {
            return new ResponseEntity<>("error: Delete unsuccessful", HttpStatus.CONFLICT);
        } catch (GitAPIException | IOException e) {
            return new ResponseEntity<>("error: Delete unsuccessful", HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{RepositoryId}/{branchName}/{NewBranchName}/RenameBranch")
    public ResponseEntity<String> RenameBranch(
            @PathVariable("RepositoryId") String RepositoryId,
            @PathVariable("branchName") String branchName,
            @PathVariable("NewBranchName") String newBranchName) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");

            if(repositoryExists(repository) && branchExists(RepositoryId, branchName)) {

                Git git = new Git(repository);

                git.branchRename().setOldName(branchName).setNewName(newBranchName).call();

                return new ResponseEntity<>("Branch rename Successfully", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Repository or branch does not exist for id: " + RepositoryId + "or branch: " + branchName, HttpStatus.NOT_FOUND);
            }
        } catch (java.lang.IllegalStateException elite) {
            return new ResponseEntity<>("error: Rename unsuccessful", HttpStatus.CONFLICT);
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("error: Rename unsuccessful", HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{RepositoryId}/branches")
    public List<String> getBranches(
            @PathVariable("RepositoryId") String RepositoryId) {

        List<String> branches = new ArrayList<>();;
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            if (repositoryExists(repository)) {
                Git git = new Git(repository);
                final List<Ref> branchRefs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                for (Ref ref : branchRefs) {
                    String name = ref.getName();
                    branches.add(name);
                }
                return branches;
            }
            else {
                branches.add("Error occurred : Repository does not exist");
                return branches;
            }
        } catch (GitAPIException | IOException exception) {
            branches.add("Error occurred : could not show branches");
            return branches;
        }
    }

    @Hidden
    @PostMapping("/{RepositoryId}/create")
    public ResponseEntity<GitCommit> createRepository(
            @PathVariable("RepositoryId") String RepositoryId,
            @RequestBody(required = false) GitCommit commitModel) {
        try {
            List<GitCommit> refObject = gitServiceFractal.CreateRepository(commitModel, RepositoryId);

            return new ResponseEntity<>(refObject.get(0), HttpStatus.CREATED);
        } catch (java.lang.IllegalStateException exception) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{RepositoryId}/directories")
    public ResponseEntity<List<Files>> listDirectory(@PathVariable("RepositoryId") String RepositoryId) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            if (repositoryExists(repository)) {
                List<Files> directories = new ArrayList<>();
                String basePath = gitServiceFractal.getBaseDirectory(RepositoryId);
                File directory = new File(basePath);
                File[] files = directory.listFiles();
                for (File dir : files) {
                    if (!dir.getName().equalsIgnoreCase(".git") && dir.isDirectory())
                        directories.add(new Files(dir.getName(), dir.isDirectory(), dir.getPath()));
                }
                if (directories.isEmpty()) return new ResponseEntity<>(HttpStatus.OK);
                else return new ResponseEntity<>(directories, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (InvalidRefNameException | NullPointerException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

        @GetMapping("/{RepositoryId}/files")
    public ResponseEntity<List<Files>> listFiles(@PathVariable("RepositoryId") String RepositoryId) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            if (repositoryExists(repository)) {
                List<Files> files = new ArrayList<>();
                String basePath = gitServiceFractal.getBaseDirectory(RepositoryId);
                File file = new File(basePath);
                File[] tempFiles = file.listFiles();
                for (File aFile : tempFiles) {
                    if (!aFile.getName().equalsIgnoreCase(".git")) {
                        files.add(new Files(aFile.getName(), aFile.isDirectory(), aFile.getPath()));
                    }
                }
                if (files.isEmpty()) return new ResponseEntity<>(HttpStatus.OK);
                else return new ResponseEntity<>(files, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch(NullPointerException | InvalidRefNameException | IOException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/{RepositoryId}/{branchName}/logs")
    public ResponseEntity<List<GitLog>> listLog(@PathVariable("RepositoryId") String RepositoryId, @PathVariable("branchName") String branchName) {
        List<GitLog> listLog = new ArrayList<>();
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branchName);
            if(repositoryExists(repository) && branchExists(RepositoryId, branchName)) {
                Git gitCommand = new Git(repository);
                Iterable<RevCommit> logs = gitCommand.log().call();
                for (RevCommit log : logs) {
                    StringBuilder sb = new StringBuilder();

                    listLog.add(new GitLog(
                            log.toObjectId().getName(),
                            log.getCommitterIdent().getName(),
                            log.getCommitterIdent().getEmailAddress(),
                            log.toString(),
                            log.getFullMessage()
                    ));
                }

                repository.close();
                return new ResponseEntity<>(listLog, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(listLog, HttpStatus.NO_CONTENT);
            }
        } catch (IOException | GitAPIException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/{RepositoryId}/{branchName}/diff")
    public ResponseEntity<String> listDiff(
            @PathVariable("RepositoryId") String RepositoryId,
            @PathVariable("branchName") String branchName,
            @RequestParam(name = "oldCommit") String oldCommit,
            @RequestParam(name = "newCommit") String newCommit) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branchName);
            if (repositoryExists(repository) && branchExists(RepositoryId, branchName)) {
                Git gitCommand = new Git(repository);
                List<DiffEntry> diffEntries = listDiff(repository, gitCommand, oldCommit, newCommit);
                StringBuilder sb = new StringBuilder();
                for (DiffEntry entry : diffEntries) {
                    sb.append(entry.getChangeType().toString())
                            .append(" : ")
                            .append(
                                    entry.getOldPath().equals(entry.getNewPath()) ? entry.getNewPath() : entry.getOldPath()
                                            + " -> " + entry.getNewPath()
                            );

                    OutputStream output = new OutputStream() {
                        StringBuilder builder = new StringBuilder();

                        @Override
                        public void write(int b) throws IOException {
                            builder.append((char) b);
                        }

                        @Override
                        public String toString() {
                            return this.builder.toString();
                        }
                    };

                    try (DiffFormatter formatter = new DiffFormatter(output)) {
                        formatter.setRepository(repository);
                        formatter.format(entry);
                    }
                    sb.append("\n").append(output);
                }
                return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Error: Repository or branch does not exist", HttpStatus.OK);
            }
        } catch (IOException | GitAPIException | NullPointerException e) {
            return new ResponseEntity<>("Error: Conflicts in commit for the Repository Id " + RepositoryId, HttpStatus.CONFLICT);
        }
    }


    private static List<DiffEntry> listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
        return diffs;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //no inspection Duplicates

        CanonicalTreeParser treeParser = null;

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();
            return treeParser;
        }
    }


    @SneakyThrows
    @GetMapping(value = "/{RepositoryId}/{branchName}/open")
    Repository open(HttpServletRequest httpRequest, @PathVariable("RepositoryId") String RepositoryId, @PathVariable("branchName") String branchName) throws JGitInternalException {

        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branchName);
            if (repositoryExists(repository) && branchExists(RepositoryId, branchName)) {
                if (repository.getRefDatabase().exactRef("HEAD") == null) {
                    throw new ServiceNotAuthorizedException();
                }
                return repository;
            } else {
                return null;
            }
        } catch (ServiceNotAuthorizedException e) {
            e.printStackTrace();
            return null;
        }
    }


    @SneakyThrows
    @GetMapping("/{RepositoryId}/{branchName}/FileContent")
    Map<String, String> viewFileContents(HttpServletRequest httpRequest,
                                         HttpServletResponse response,
                                         @PathVariable("RepositoryId") String RepositoryId,
                                         @PathVariable("branchName") String branchName,
                                         @RequestParam("filePath") String filePath)
            throws JGitInternalException {


        try {

            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branchName);
            if (repositoryExists(repository) && branchExists(RepositoryId, branchName)) {
//                Ref head = repository.exactRef(filesContentsRequest.getRefs());
                // find the HEAD
                ObjectId lastCommitId = repository.resolve(Constants.HEAD);

                // a RevWalk allows to walk over commits based on some filtering that is defined
                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(lastCommitId);
                    // and using commits tree find the path
                    RevTree tree = commit.getTree();
//                    System.out.println("Having tree: " + tree);

                    // now try to find a specific file
                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        treeWalk.setFilter(PathFilter.create(filePath));
                        if (!treeWalk.next()) {
                            throw new IllegalStateException("Did not find expected file " + filePath);
                        }

                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);

                        String binaryAsAString = Base64.encodeBase64String(loader.getBytes());

                        Map<String, String> fileContent = new HashMap<>();
                        fileContent.put("fileContents.b64", binaryAsAString);
//                        fileContent.put("fileContents.txt", new String(loader.getBytes(), StandardCharsets.UTF_8));
                        fileContent.put("size", String.valueOf(loader.getSize()));
                        fileContent.put("type", String.valueOf(loader.getType()));
                        fileContent.put("objectId", objectId.getName());
                        fileContent.put("path", treeWalk.getPathString());
                        fileContent.put("mode", treeWalk.getFileMode().toString());
                        fileContent.put("name", treeWalk.getNameString());

                        return fileContent;
                    }
                }
            } else {
                Map<String, String> errorTemplate = new HashMap<>();
                errorTemplate.put("error", "Repository or branch does not exist.");
                return errorTemplate;
            }
        } catch (JGitInternalException | IllegalStateException e) {
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error","Did not find expected file " + filePath);
            return errorTemplate;
        }
    }

    @SneakyThrows
    @GetMapping("/{RepositoryId}/{branch}/tree")
    List<Object> treeView(HttpServletRequest httpRequest,
                          HttpServletResponse response,
                          @PathVariable("RepositoryId") String RepositoryId,
                          @PathVariable("branch") String branchName
    )
            throws JGitInternalException {


          // below is only temporary for testing so you don't have to search ID every test.
//        FolderModel folderModel = folderRepository.getByName("TestRepo");
//        Repository repository = gitServiceFractal.getRepository("/repositories/" + folderModel.id.toString(), branchName);
//        System.out.println(folderModel.id.toString());

        FolderModel folderModel = folderRepository.getById(UUID.fromString(RepositoryId));
        Repository repository = gitServiceFractal.getRepository("/repositories/" + folderModel.getId().toString(), branchName);

        if(repositoryExists(repository) && branchExists(RepositoryId, branchName)) {

            RevWalk rw = new RevWalk(repository);

            try (TreeWalk tw = new TreeWalk(repository)) {
                RevCommit commitToCheck = rw.parseCommit(repository.resolve(Constants.HEAD));
                tw.addTree(commitToCheck.getTree());
                tw.addTree(new DirCacheIterator(repository.readDirCache()));

                tw.addTree(new FileTreeIterator(repository));
                tw.setRecursive(false);

                List<Folder> folders = new ArrayList<>();

                while (tw.next()) {

                    Folder folder = new Folder();

                if (tw.isSubtree()) {
                    folder.setType("folder");
                    tw.enterSubtree();
                } else {
                    folder.setType("file");
                }

                folder.setPath(tw.getPathString());
                folder.setDepth(tw.getDepth());
                folder.setName(tw.getNameString());
                folder.setMode(tw.getFileMode().toString());

                    folders.add(folder);
                }
                return iterativeFileTreeExplorer(folders);

            } catch (JGitInternalException | IllegalArgumentException e) {
                List<Object> errorTemplate = new ArrayList<>();
                errorTemplate.add("Error: Could not retrieve data for the Repository" + RepositoryId);
                return errorTemplate;
            }
        }
        else {
            List<Object> errorTemplate = new ArrayList<>();
            errorTemplate.add("Error: Repository or branchName does not exist");
            return errorTemplate;
        }
    }

    private List<Object> iterativeFileTreeExplorer(List<Folder> completeDirectoryList) {
        List<Object> directoryForest = new ArrayList<>();

        Stack<String> paths = new Stack<>();
        Stack<List<Object>> toPopulate = new Stack<>();
        Stack<String> keys = new Stack<>();

        toPopulate.push(directoryForest);
        keys.push("0");

        for (Folder folder : completeDirectoryList) {

            while (!paths.isEmpty() && !folder.getPath().startsWith(paths.peek())) {
                paths.pop();
                toPopulate.pop();
                keys.pop();
            }

            List<Object> whereToPut = toPopulate.peek();

            folder.setLeaf(Objects.equals(folder.getType(), "file"));
            folder.setKey(keys.peek() + "-" + whereToPut.size());

            Map<String, Object> vertex = new TreeMap<>(((Comparator<String>) String::compareTo).reversed());
            vertex.put("key", folder.getKey());
            vertex.put("title", folder.getName());
            vertex.put("type", folder.isLeaf() ? "file" : "folder");
            vertex.put("path", folder.getPath());
            vertex.put("depth", folder.getDepth());
            vertex.put("isLeaf", folder.isLeaf());

            if (!folder.isLeaf()) {
                List<Object> children = new ArrayList<>();
                paths.push(folder.getPath());
                toPopulate.push(children);
                keys.push(folder.getKey());
                vertex.put("children", children);
            }

            else {
                if(folder.getName().startsWith(".")) {
                    vertex.put("isHidden", true);
                }
                else {
                    vertex.put("isHidden", false);
                }
            }
            
            whereToPut.add(vertex);
        }

        return directoryForest;
    }


    @DeleteMapping("/{RepositoryId}/DeleteBranches")
    ResponseEntity<List<String>> deleteBranches(Git git, Collection<String> deleteBranches,
                                                @PathVariable("RepositoryId") String RepositoryId) {

        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, "main");
            if (repositoryExists(repository)) {
                DeleteBranchCommand deleteBranchCommand = git.branchDelete()
                        .setBranchNames(deleteBranches.toArray(new String[0]))
                        .setForce(true);
                List<String> resultList = deleteBranchCommand.call();
                return new ResponseEntity<>(resultList, HttpStatus.OK);
            } else {
                List<String> errorTemplate = new ArrayList<>();
                errorTemplate.add("Error: Repository does not exist");
                return new ResponseEntity<>(errorTemplate, HttpStatus.OK);
            }
        } catch (IOException | GitAPIException | NullPointerException | JGitInternalException | IllegalStateException e) {
            List<String> errorTemplate = new ArrayList<>();
            errorTemplate.add("error: could not delete branches for the Repository Id " + RepositoryId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.CONFLICT);
        }
    }

    @Hidden // not used
    @Deprecated
    @SneakyThrows
    @PostMapping("/{RepositoryId}/{branch}/Commit")
    ResponseEntity<String> commit(HttpServletRequest httpRequest,
                                  HttpServletResponse response,
                                  @PathVariable("RepositoryId") String RepositoryId,
                                  @PathVariable("branch") String branchName,
                                  @RequestBody GitCommit commitModel) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branchName);
            Git gitCommand = new Git(repository);
            if(repositoryExists(repository) && branchExists(RepositoryId, branchName)) {

                System.out.println(RepositoryId);
                gitCommand.add().addFilepattern(".").call();

                CommitCommand commitCommand = gitCommand.commit().setMessage(commitModel.getMessage())
                        .setCommitter(commitModel.getUsername(), commitModel.getEmail());
                commitCommand.call();

                return new ResponseEntity<>("Commit successful", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Error: repository or branch does not exist", HttpStatus.OK);
            }
        } catch (GitAPIException | JGitInternalException | NullPointerException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Commit unsuccessful", HttpStatus.NO_CONTENT);
        }
    }


    @SneakyThrows
    @PostMapping("/{RepositoryId}/{branch}/Save")
    ResponseEntity<String> save(HttpServletRequest httpRequest,
                                  HttpServletResponse response,
                                  @PathVariable("RepositoryId") String RepositoryId,
                                  @PathVariable("branch") String branch,
//                                @RequestBody GitCommit commitModel,
                                  @RequestBody ArrayList<Map<String, Object>> userInput

    ) throws JGitInternalException {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + RepositoryId, branch);
            Git gitCommand = new Git(repository);

            if(repositoryExists(repository) && branchExists(RepositoryId, branch)) {

                CheckoutCommand checkoutCommand = gitCommand.checkout().setName(branch);
                checkoutCommand.call();

                for (Map<String, Object> contents : userInput) {

                    String fileContent = (String) contents.get("fileContent");
                    byte[] valueDecoded = Base64.decodeBase64(fileContent.getBytes());
                    String folderPath = (String) contents.get("filePath");
                    String fileName = folderPath;
                    int index = folderPath.lastIndexOf('/');
                    File file;
                    if(index != -1) {
                        folderPath = folderPath.substring(0, index);
                        fileName = ((String) contents.get("filePath")).substring(index + 1);
                        file = new File(repository.getDirectory().getParent(), folderPath);
                        boolean b = file.mkdirs();
                    }

                    file = new File(repository.getDirectory().getParent(), (String) contents.get("filePath"));
                    FileWriter fileWriter = new FileWriter(file.getPath());
                    fileWriter.write(new String(valueDecoded));
                    fileWriter.close();
                    gitCommand.add().addFilepattern(fileName).call();
                }
                CommitCommand commitCommand = gitCommand.commit().setMessage(new Date().toString())
                        .setCommitter("String", "String");
                commitCommand.call();

                return new ResponseEntity<>("Save successful", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Repository or branch does not exist", HttpStatus.OK);
            }
        } catch (IOException | JGitInternalException| GitAPIException | NullPointerException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Save unsuccessful", HttpStatus.NO_CONTENT);
        }
    }

    @SneakyThrows
    @GetMapping("/{RepositoryId}/{MergeBranch}/{MergeInto}/merge")
    public ResponseEntity<String> merge(
            @PathVariable("RepositoryId") String repositoryId,
            @PathVariable("MergeBranch") String mergeBranch,
            @PathVariable("MergeInto") String mergeBranchInto) {
        try {
            Repository repository = gitServiceFractal.getRepository("/repositories/" + repositoryId, mergeBranch);
            Git gitCommand = new Git(repository);
            if(repositoryExists(repository) && branchExists(repositoryId, mergeBranch) && branchExists(repositoryId, mergeBranchInto)) {

                CheckoutCommand checkoutCommand = gitCommand.checkout().setName(mergeBranchInto);
                checkoutCommand.call();

                Ref ref = gitCommand.getRepository().findRef("refs/heads/" + mergeBranch);

                MergeCommand mergeCommand = gitCommand.merge().include(ref);
                mergeCommand.call();

                return new ResponseEntity<>("Merge successful", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Repository or branch does not exist", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Merge unsuccessful", HttpStatus.NO_CONTENT);
        }
    }

    private boolean repositoryExists(Repository repository) {
        try {
            if (!repository.getObjectDatabase().exists()) {
                throw new RepositoryNotFoundException("Repo not found");
            }
        } catch (RepositoryNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean branchExists(String RepositoryId, String branchName) {
        List<String> branches = getBranches(RepositoryId);

        for (String branch : branches) {
            if(branch.substring(branch.lastIndexOf('/') + 1).equals(branchName)) {
                return true;
            }
        }
        return false;
    }

//    @ExceptionHandler(NoSuchElementFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<String> handleNoSuchElementFoundException(
//            NoSuchElementFoundException exception
//    ) {
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(exception.getMessage());
//    }

}
