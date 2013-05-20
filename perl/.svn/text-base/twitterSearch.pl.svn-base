#!/usr/bin/perl

use LWP::Simple qw($ua get);

my $ua = LWP::UserAgent->new;

$ua->timeout(3000);
$ua->agent('Mozilla/Firefox');

$outputFolder = "../twitter_dump/";
mkdir($outputFolder);

@dictionary = ("mozilla","firefox","fox fire","fire fox","foxfire","internet explorer","chrome","ie9","ie8","webkit","gecko","internetexplorer");
@dictionary = ("internet explorer","chrome","ie9","ie8","webkit","gecko","internetexplorer");

$baseUrl = "http://search.twitter.com/search.atom?lang=en"; 
foreach $term(@dictionary) {
	$outputFile = $outputFolder . $term . ".txt";
	
	open(A,">>$outputFile");	
	for ($i=1; $i < 16; $i++) {
		$url = $baseUrl . "&q=" . $term . "&page=" . $i . "&rpp=100";
#		$url = get 'http://www.DevDaily.com/';
		print $url . "\n";
		my $html = get $url || die "Timed out!";
		print A $html;
		sleep(1); #in seconds
	}
	
	close(A);
}
