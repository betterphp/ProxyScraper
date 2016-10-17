<?php

header('Content-Type: application/json');

$forwarded_for = null;

if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])){
	$forwarded_for = $_SERVER['HTTP_X_FORWARDED_FOR'];
}else if (!empty($_SERVER['HTTP_FORWARDED'])){
	preg_match('#for=([0-9\.]+);#', $_SERVER['HTTP_FORWARDED'], $matches);

	$forwarded_for = (isset($matches[1])) ? $matches[1] : null;
}else if (!empty($_SERVER['HTTP_X_REAL_IP'])){
	$forwarded_for = $_SERVER['HTTP_X_REAL_IP'];
}

echo json_encode([
	'remote_addr' => $_SERVER['REMOTE_ADDR'],
	'forwarded_for' => $forwarded_for,
], JSON_PRETTY_PRINT);
