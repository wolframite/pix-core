package org.m18.pix.models.repositories;

import javax.transaction.Transactional;
import org.m18.pix.models.entities.Image;
import org.springframework.stereotype.Component;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Wolfram Huesken <woh@m18.io>
 */
@Component
@Transactional(rollbackOn = Exception.class)
public interface ImageRepository extends CrudRepository<Image, Integer> {}
