package com.user.api.userProperties;

import com.user.api.userProperties.model.UserProperties;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPropertiesRepository extends JpaRepository<UserProperties, Long> {

}