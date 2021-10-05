package io.bosler;

import io.bosler.fractal.library.models.GitCommit;
import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.models.DatasetModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.models.Groups;
import io.bosler.passport.library.models.Users;
import io.bosler.passport.library.service.GroupService;
import io.bosler.passport.library.service.SSOService;
import io.bosler.passport.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class SampleData {

    private final GitService gitServiceSampleData;
    private final DatasetRepository datasetRepository;


    // The below is to create Bosler, first it deletes user.home/bosler directory and then adds data and folders
    @Bean
    CommandLineRunner run(UserService userService, GroupService groupService, SSOService ssoService, FolderRepository folderRepository) {
        return args -> {

            // CleanUp first
            File boslerRepositoryHome = new File(System.getProperty("user.home") + "/bosler/repositories");

            if (boslerRepositoryHome.exists()) {
                FileUtils.deleteDirectory(boslerRepositoryHome);
            }

            File boslerDatasetHome = new File(System.getProperty("user.home") + "/bosler/datasets");

            if (boslerDatasetHome.exists()) {
                FileUtils.deleteDirectory(boslerDatasetHome);
            }
            boslerDatasetHome.mkdirs();

            File boslerUploads = new File(System.getProperty("user.home") + "/bosler/uploads");

            if (boslerUploads.exists()) {
                FileUtils.deleteDirectory(boslerUploads);
            }
            boslerUploads.mkdirs();

            groupService.saveGroup(new Groups(null, "TEAM-OWN", "test", null, null, null, null, null, null, null, null));
            groupService.saveGroup(new Groups(null, "TEAM-EDIT", "test", null, null, null, null, null, null, null, null));
            groupService.saveGroup(new Groups(null, "TEAM-VIEW", "test", null, null, null, null, null, null, null, null));


            userService.saveUser(new Users(null, "John Travolta", "john", "1234"));
            userService.saveUser(new Users(null, "Will Smith", "will", "1234"));
            userService.saveUser(new Users(null, "Jim Carry", "jim", "1234"));
            userService.saveUser(new Users(null, "Arnold Schwarzenegger", "arnold", "1234"));

            groupService.addUserToGroupOwners("john", "TEAM-OWN");
            groupService.addUserToGroupAdministrators("john", "TEAM-EDIT");
            groupService.addUserToGroupMembers("john", "TEAM-VIEW");
//			userService.addRoleToUser("john", "ROLE_MANAGER");
//			userService.addRoleToUser("will", "ROLE_MANAGER");
//			userService.addRoleToUser("jim", "ROLE_ADMIN");
//			userService.addRoleToUser("arnold", "ROLE_SUPER_ADMIN");
//			userService.addRoleToUser("arnold", "ROLE_ADMIN");
//			userService.addRoleToUser("arnold", "ROLE_USER");

            Date date = new Date();
            FolderModel project = folderRepository.save(new FolderModel(null,
                    "Quality",
                    "This is a test project",
                    "project",
                    new UUID(0, 0),
                    "active",
                    new Date(),
                    new Date(),
                    "Bosler",
                    "Bosler"
            ));

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

            FolderModel sampleDataset = folderRepository.save(new FolderModel(null,
                    "Dataset",
                    "This is a dataset for testing purposes only.",
                    "dataset",
                    dataFolder.id,
                    "active",
                    new Date(),
                    new Date(),
                    "Bosler",
                    "Bosler"
            ));

            datasetRepository.save(new DatasetModel(sampleDataset.id, sampleDataset.name,
                    null,
                    new Date(),
                    new Date(),
                    "Bosler",
                    "Bosler"
            ));


            FolderModel repository = folderRepository.save(new FolderModel(null,
                    "TestRepo",
                    "This is a repository for testing",
                    "repository",
                    logicFolder.id,
                    "active",
                    new Date(),
                    new Date(),
                    "Bosler",
                    "Bosler"
            ));

            GitCommit commitModel = new GitCommit();

            commitModel.setUsername("Bosler");
            commitModel.setEmail("rakesh@bosler.io");
            commitModel.setMessage("Sample Repository : Automatically created repository");

            List<GitCommit> refObject = gitServiceSampleData.CreateRepository(commitModel, repository.id.toString());

        };
    }
}
