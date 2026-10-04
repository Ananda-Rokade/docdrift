package com.docdrift.repository;

import com.docdrift.entity.Scan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {}
