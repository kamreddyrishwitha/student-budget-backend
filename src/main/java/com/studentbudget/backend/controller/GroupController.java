package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.AddGroupMemberRequest;
import com.studentbudget.backend.dto.GroupRequest;
import com.studentbudget.backend.dto.GroupResponse;
import com.studentbudget.backend.service.GroupService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(
            GroupService groupService) {

        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse>
    createGroup(
            @Valid
            @RequestBody
            GroupRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        groupService.createGroup(
                                principal.getName(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>>
    getMyGroups(
            Principal principal) {

        return ResponseEntity.ok(
                groupService.getMyGroups(
                        principal.getName()
                )
        );
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse>
    getGroupById(
            @PathVariable Long groupId,
            Principal principal) {

        return ResponseEntity.ok(
                groupService.getGroupById(
                        principal.getName(),
                        groupId
                )
        );
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse>
    updateGroup(
            @PathVariable Long groupId,
            @Valid
            @RequestBody
            GroupRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                groupService.updateGroup(
                        principal.getName(),
                        groupId,
                        request
                )
        );
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse>
    addMember(
            @PathVariable Long groupId,
            @Valid
            @RequestBody
            AddGroupMemberRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                groupService.addMember(
                        principal.getName(),
                        groupId,
                        request
                )
        );
    }

    @DeleteMapping(
            "/{groupId}/members/{userId}"
    )
    public ResponseEntity<GroupResponse>
    removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            Principal principal) {

        return ResponseEntity.ok(
                groupService.removeMember(
                        principal.getName(),
                        groupId,
                        userId
                )
        );
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>>
    deleteGroup(
            @PathVariable Long groupId,
            Principal principal) {

        groupService.deleteGroup(
                principal.getName(),
                groupId
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Group deleted successfully"
                )
        );
    }
}