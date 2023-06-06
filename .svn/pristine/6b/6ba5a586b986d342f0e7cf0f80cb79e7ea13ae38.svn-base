package ann;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import util.Log;
import util.Settings;
import util.bio.Annotation;
import util.bio.GFFException;
import util.bio.GFFParser;
import util.bio.GFFeature;
import util.bio.Gene;
import util.bio.Intron;

public class Annotator {
	GFFPrinter gffp;
	SamReader in;
	SAMRecordIterator iter;
	SAMRecord cur;
	String chr_id;
	ChrCoverage chrCov;
	IndexedFastaSequenceFile fasta;
	Annotation foreignAnn = null;
	HashSet<String> chrs = new HashSet<>();
	HashMap<String,ArrayList<Intron>> forcedIntrons;

	public Annotator() throws IOException, GFFException {
		gffp = new GFFPrinter(new PrintStream(Settings.S().getString(Settings.ANN_OUT)));
		gffp.printAnnotateHeader();
		in = SamReaderFactory.makeDefault().open(SamInputResource.of(new BufferedInputStream(new FileInputStream(Settings.S().getString(Settings.IN)),10000000)));
		iter = in.iterator();
		cur = iter.next();
		fasta = new IndexedFastaSequenceFile(new File(Settings.S().getString(Settings.FASTA)));
		if(!Settings.S().getString(Settings.ANN_FOREIGN).equals("-"))
			foreignAnn = new Annotation(Settings.S().getString(Settings.ANN_FOREIGN));
		if(!Settings.S().getString(Settings.FORSED_INTRON_SET).equals("-")){
			forcedIntrons = new HashMap<>();
			GFFParser p = new GFFParser(Settings.S().getString(Settings.FORSED_INTRON_SET));
			for(GFFeature f = p.next();f!=null;f = p.next()){
				if(f.feature.equals("intron")){
					ArrayList<Intron> ints = forcedIntrons.get(f.seqname);
					if(ints == null){
						ints = new ArrayList<>();
						forcedIntrons.put(f.seqname, ints);
					}
					ints.add(new Intron(f.start,f.stop,f.strand));
				}
			}
			p.close();
		}
	}
	
	public void annotate() throws IOException{
		do{
			chr_id = cur.getReferenceName();
			Log.println(chr_id);
			String seq =  new String(fasta.getSequence(chr_id).getBases());
			chrCov = new ChrCoverage(chr_id, seq);
			chrs.add(chr_id);
		}while(_annotate());
		
		// add genes from foreign annotation from chrs that do not have coverage
		if(foreignAnn != null || forcedIntrons != null){
			Set<String> annChrs = null;
			if(forcedIntrons != null)
				annChrs = forcedIntrons.keySet();
			else
				annChrs = foreignAnn.getChrIDs();
			annChrs = util.Util.diff(annChrs,chrs);
			for(String chr_id : annChrs){
				String seq =  new String(fasta.getSequence(chr_id).getBases());
				chrCov = new ChrCoverage(chr_id, seq);					
				if(forcedIntrons != null)
					chrCov.setIntrons(forcedIntrons.get(chr_id));
				else
					chrCov.addForeighAnnotation(foreignAnn.getChrAnnotation(chr_id));
				ArrayList<Gene> genes = chrCov.findGenes();
				Collections.sort(genes);
				for(Gene g : genes)
					gffp.printGene(g);
			}
		}
		gffp.close();
		Log.printStat();
	}
	
	private boolean _annotate() throws FileNotFoundException{
		for(;;){
			Log.addStat(Log.TOTAL_READS, 1);
			chrCov.read(cur);
			cur = iter.next();
			if(cur == null || !cur.getReferenceName().equals(chr_id))
				break;
		}
		if(Settings.S().getBoolean(Settings.FILL_NS))
			chrCov.fillCovInNs();
		//first add annotation, then filter introns.
		if(foreignAnn != null && forcedIntrons == null)
			chrCov.addForeighAnnotation(foreignAnn.getChrAnnotation(chr_id));
		if(forcedIntrons != null)
			chrCov.setIntrons(forcedIntrons.get(chr_id));
		else
			chrCov.filterIntrons();
		ArrayList<Gene> genes = chrCov.findGenes();
		for(Gene g : genes)
			gffp.printGene(g);
		System.gc();
		return cur != null;
	}
	
}
