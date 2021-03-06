package org.jenkinsci.plugins.p4.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.p4.PerforceScm;
import org.jenkinsci.plugins.p4.browsers.P4Browser;
import org.jenkinsci.plugins.p4.populate.GraphHybridImpl;
import org.jenkinsci.plugins.p4.populate.Populate;
import org.jenkinsci.plugins.p4.workspace.Workspace;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GlobalLibrarySCMSource extends AbstractP4SCMSource {

	private final String path;

	public String getPath() {
		return path;
	}

	@DataBoundConstructor
	public GlobalLibrarySCMSource(String credential, String charset, String path) {
		super(credential);
		this.path = path;
		setCharset(charset);
		setFormat("jenkins-library-${NODE_NAME}-${JOB_NAME}");
	}

	@Override
	protected SCMRevision retrieve(@NonNull final String thingName, @NonNull TaskListener listener)
			throws IOException, InterruptedException {
		try {
			P4Path p4Path = new P4Path(path, thingName);
			P4SCMHead head = new P4SCMHead(thingName, p4Path);
			SCMRevision revision = getRevision(head, listener);
			return revision;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	// Not used for Global Library
	@Override
	public P4Browser getBrowser() {
		return null;
	}

	@Override
	public List<P4SCMHead> getHeads(@NonNull TaskListener listener) throws Exception {
		// not used
		return new ArrayList<>();
	}

	@Override
	public List<P4SCMHead> getTags(@NonNull TaskListener listener) throws Exception {
		// not used
		return new ArrayList<>();
	}

	@Override
	public PerforceScm build(@NonNull SCMHead head, SCMRevision revision) {
		if (head instanceof P4SCMHead && revision instanceof P4SCMRevision) {
			P4SCMHead perforceHead = (P4SCMHead) head;
			P4SCMRevision perforceRevision = (P4SCMRevision) revision;

			// Build workspace from 'head' paths
			P4Path path = perforceHead.getPath();
			Workspace workspace = getWorkspace(path);

			// Build populate from revision
			String pin = perforceRevision.getRef().toString();
			Populate populate = new GraphHybridImpl(true, pin, null);
			PerforceScm scm = new PerforceScm(getCredential(), workspace, null, populate, getBrowser());
			return scm;
		} else {
			throw new IllegalArgumentException("SCMHead and/or SCMRevision not a Perforce instance!");
		}
	}

	@Extension
	@Symbol("globalLib")
	public static final class DescriptorImpl extends P4SCMSourceDescriptor {

		@Override
		public String getDisplayName() {
			return "Helix Library";
		}
	}
}
