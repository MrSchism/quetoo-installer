package org.quetoo.installer.aws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;

/**
 * Integration tests for the {@link S3BucketSync} class.
 * 
 * @author jdolan
 */
public class S3BucketSyncTest {
		
	private S3BucketSync s3BucketSync;
		
	@Before
	public void before() {
		
		final File destination = new File(FileUtils.getTempDirectory(), "quetoo");
		
		System.out.println("Syncing to desination " + destination);
		
		s3BucketSync = new S3BucketSync.Builder()
				.withHttpClient(HttpClients.createDefault())
				.withBucketName("quetoo")
				.withPredicate(s -> s.getKey().startsWith("x86_64-apple-darwin"))
				.withMapper(s -> new File(s.getKey().replace("x86_64-apple-darwin", "")))
				.withDestination(destination)
				.build();
		
		FileUtils.deleteQuietly(destination);
	}
	
	private void onSync(final File file) {
		assertTrue(file.exists());
		System.out.println("Updated " + file);
	}
	
	private void onError(final Throwable t) {
		t.printStackTrace(System.err);
		fail();
	}
	
	private void onComplete() {
		System.out.println("Complete");
	}

	@Test
	public void sync() throws IOException {
		
		Observable<File> files = s3BucketSync.sync();
		
		files.subscribe(this::onSync, this::onError, this::onComplete);
		List<File> result = files.toList().blockingGet();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}
}
