package io.bosler.fractal.library.repository;

import io.bosler.fractal.library.models.GitCommit;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GitService {

    private String uriRepository(String project) {
        return getBaseDirectory(project) +
                File.separator + ".git";
    }

    public String getBaseDirectory(String projectName) {
        return System.getProperty("user.home") +
                File.separator + "bosler" +
                File.separator + projectName;
    }

    public File getBaseGitDir(String projectName) {
        return new File(uriRepository(projectName));
    }

    public List<File> createFolder(String projectName, List<String> folderNames) throws IOException {
        List<File> gitkeep = new ArrayList<>();
        for (String name : folderNames) {
            String folderLocation = getBaseDirectory(projectName) +
                    File.separator +
                    name;
            File folder = new File(folderLocation);
            boolean dir = folder.mkdirs();
            if (dir) {
                File gitKeep = new File(
                        folderLocation +
                                File.separator +
                                ".gitkeep");
                gitKeep.createNewFile();
                gitkeep.add(gitKeep);
            }
        }
        return gitkeep;
    }


    public String uriAbsoluteRepository(String project) {
        return System.getProperty("user.home") +
                File.separator + project;
    }

    public Repository getRepository(String repositoryId, String branch) throws IOException, InvalidRefNameException {
        return new FileRepositoryBuilder()
                .setGitDir(getBaseGitDir(repositoryId))
                .readEnvironment()
                .setup()
                .setInitialBranch(branch)
                .findGitDir()
                .build();
    }

    public List<GitCommit> CreateRepository(GitCommit commitModel, String RepositoryId) throws IOException, GitAPIException {

        if(getBaseGitDir(RepositoryId).exists()){
            throw new IOException("Repository already exists with name " + RepositoryId);
        }
        Repository repository = getRepository("/repositories/" + RepositoryId, "main");
//            created git repository
        repository.create();
        Git gitCommand = new Git(repository);

//            list folder created
        List<File> folders = createFolder("/repositories/" + RepositoryId, Arrays.asList("bin", "etc"));

//       create requirements.txt
        File requirements = new File(repository.getDirectory().getParent(), "requirements.txt");

        FileWriter requirementsWriter = new FileWriter(requirements.getPath());
        requirementsWriter.write("coverage>=4.0.3\n" +
                "engarde>=0.3.1\n" +
                "ipython>=4.1.2\n" +
                "jupyter>=1.0.0\n" +
                "matplotlib>=1.5.1\n" +
                "notebook>=4.1.0\n" +
                "numpy>=1.10.4\n" +
                "pandas>=0.17.1\n" +
                "seaborn>=0.7.0\n" +
                "q>=2.6\n" +
                "python-dotenv>=0.5.0\n" +
                "watermark>=1.3.0\n" +
                "pytest>=2.9.2\n" +
                "tqdm\n" +
                "numpy\n" +
                "pandas\n" +
                "matplotlib\n" +
                "watermark\n" +
                "scikit-learn\n" +
                "scipy\n" +
                "nbdime\n" +
                "runipy");

        requirementsWriter.close();

        File initPy = new File(repository.getDirectory().getParent(), "bin/__init__.py");
        File datasetPipeline = new File(repository.getDirectory().getParent(), "bin/dataset_pipeline.py");
        FileWriter datasetPipelineWriter = new FileWriter(datasetPipeline.getPath());
        datasetPipelineWriter.write("from pyspark.sql import SparkSession\n" +
                "\n" +
                "spark = SparkSession.builder\n" +
                "        .main(\"local[*]\")\n" +
                "        .appName('PySpark_Tutorial')\n" +
                "        .getOrCreate()\n" +
                "print(\"hello spark\")\n");

        datasetPipelineWriter.close();

        File readme = new File(repository.getDirectory().getParent(), "README.md");
        FileWriter readmeWriter = new FileWriter(readme.getPath());
        readmeWriter.write("# This is a template for data pipeline in Python\n" +
                "\n" +
                "You should use bin folder for code and etc for configuration.");

        readmeWriter.close();


//            git add .
        gitCommand.add().addFilepattern(".").call();

//            git commit -m "init project"
        CommitCommand commitCommand = gitCommand.commit().setMessage(commitModel.getMessage())
                .setCommitter(commitModel.getUsername(), commitModel.getEmail());
        commitCommand.call();

//            git log
        Iterable<RevCommit> listLog = gitCommand.log().setMaxCount(1).call();
        List<GitCommit> refObject = new ArrayList<>();
        for (RevCommit log : listLog) {
            GitCommit commit = new GitCommit();
            commit.setId(log.getId().getName());
            commit.setMessage(log.getFullMessage());
            commit.setUsername(log.getCommitterIdent().getName());
            commit.setEmail(log.getCommitterIdent().getEmailAddress());
            refObject.add(commit);
        }
        repository.close();

        return refObject;
    }
}
