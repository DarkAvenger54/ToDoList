package pl.edu.wit.todolist.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.wit.todolist.entity.GroupEntity;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
}